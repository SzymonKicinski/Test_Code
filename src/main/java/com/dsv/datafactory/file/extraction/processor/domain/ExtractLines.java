package com.dsv.datafactory.file.extraction.processor.domain;

import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.logging.ECSLoggerProvider;

import com.dsv.datafactory.model.Document;
import com.dsv.datafactory.model.Line;
import com.dsv.datafactory.model.MetaData;
import com.dsv.datafactory.model.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.entity.StringEntity;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.stream.Collectors;

import com.dsv.datafactory.model.*;
import com.dsv.logger.ECSLogger;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.*;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

// @Restcontroller
// Spring in general
// Remove unused code + unnecessary comments - the code itself should be a comment
// static to logger
// try-resources-catch
// added support for null -> empty objects
//
public class ExtractLines {
    private final static ECSLogger logger = ECSLoggerProvider.getLogger(ExtractLines.class.getName());

    String lineServiceUrl;
    String startNumberOfClasses;
    String goodnessOfFit;

    @Inject
    public ExtractLines(Config config) {
        this.lineServiceUrl = config.lineServiceUrl;
        this.startNumberOfClasses = config.startNumberOfClasses;
        this.goodnessOfFit = config.goodnessOfFit;
    }

    public void generateInputFromDocument(Document document) throws IOException {
        try {
            for (Page page : document.getPages()) {
                JsonObject requestValue = new JsonObject();
                if (page.getLines().isEmpty()) {
                    logger.warn("Skipping page, no lines detected.");
                    continue;
                }
                List<BoundingBox> wordBoxes = page.getLines().get(0).getWords().stream().map(Word::getBoundingBox).collect(Collectors.toList());

                List<String> strBoxes = wordBoxes.stream()
                        .map(BoundingBox::serialize)
                        .collect(Collectors.toList());

                JsonArray values = JsonParser.parseString(strBoxes.toString()).getAsJsonArray();
                startNumberOfClasses = String.valueOf(Math.round(2 * Math.log(values.size())));
                requestValue.add("values", values);
                requestValue.addProperty("start_num_of_class", startNumberOfClasses);
                requestValue.addProperty("min_goodness_of_fit", goodnessOfFit);
                requestValue.addProperty("type", "customs");
                String results = submitRequest(requestValue.toString());
                parseResults(results, page);
            }
        } catch (Exception e) {
            logger.error("Error extracting lines in LineExtractor", e);
        }
    }

    public void parseResults(String results, Page originalPage) {
        List<Line> newLines = new ArrayList<>();
        int originalNumWords = originalPage.getLines().get(0).getWords().size();
        JenksResponse jenks = deserializeResponse(results);
        for (Cluster clust : jenks.getValues()) {
            int lineNumber = clust.getLineNumber();
            List<Integer> meansForLine = clust.getYMeans();
            Line newLine = new Line();
            List<Word> newWords = originalPage.getLines().get(0).getWords().stream().filter(x -> meansForLine.stream().anyMatch(i -> i.equals(x.getyMean()))).collect(Collectors.toList());
            newLine.setLineNumber(lineNumber);
            newLine.setWords(newWords);
            newLines.add(newLine);
        }
        int newWords = (int) newLines.stream().map(Line::getWords).mapToLong(List::size).sum();
        if (!newLines.isEmpty() && originalNumWords == newWords) {
            originalPage.setLines(newLines);
        } else {
            originalPage.setLines(newLines);
            logger.warn("Mismatch in word count: original=" + originalNumWords + ", after clustering=" + newWords);
        }
    }

    String submitRequest(String jsonRequest) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(lineServiceUrl);
            post.setEntity(new StringEntity(jsonRequest, ContentType.APPLICATION_JSON));

            try (CloseableHttpResponse response = httpClient.execute(post)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode != 200) {
                    logger.error("Request failed with status: " + statusCode + ", response: " + responseBody);
                    throw new IOException("HTTP error: " + statusCode);
                }
                return responseBody;
            }
        }
    }

    JenksResponse deserializeResponse(String jsonResponse) {
        if (jsonResponse == null || jsonResponse.isEmpty()) {
            logger.warn("Received empty response for Jenks clustering");
            return new JenksResponse();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(jsonResponse.getBytes(StandardCharsets.UTF_8), JenksResponse.class);
        } catch (Exception e) {
            logger.warn("Failed to parse JenksResponse: " + e.getMessage());
            return new JenksResponse();
        }
    }
}
