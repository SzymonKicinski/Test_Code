package com.dsv.datafactory.file.extraction.processor.domain.ocr;

import com.dsv.datafactory.file.extraction.processor.domain.ocr.orchestrator.*;
import com.dsv.datafactory.file.extraction.processor.logging.ECSLoggerProvider;
import com.dsv.datafactory.file.extraction.processor.models.GooglePage;
import com.dsv.datafactory.file.extraction.processor.models.GoogleVisionResponse;
import com.dsv.datafactory.model.*;
import com.dsv.datafactory.model.Word;
import com.dsv.logger.ECSLogger;

import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.Image;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
// Add spring annotations
// Add try-resources-catch
// Add Optional
// Consider whether the approach of writing an Orchestrator for CORs is a good idea?
// If Orchestrators were only used by OCR, then limit other classes from accessing them?
// Maybe use List instead of ArrayList? -> More flexibility in the future
// Use streams where possible
public class GoogleOcr implements ExtractPageInterface {

    private final static ECSLogger logger = ECSLoggerProvider.getLogger(GoogleOcrRefactor.GoogleOcr.class.getName());
    private final Feature feature = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
    private final ImageOrchestrator imageOrchestrator;
    private final LineOrchestrator lineOrchestrator;
    private final LanguageOrchestrator languageOrchestrator;
    private final WordOrchestrator wordOrchestrator;


    public GoogleOcr() {
        this.imageOrchestrator = new ImageOrchestrator();
        this.lineOrchestrator = new LineOrchestrator();
        this.languageOrchestrator = new LanguageOrchestrator();
        this.wordOrchestrator = new WordOrchestrator();
    }


    public Document generateDocument(List<String> imagePaths, String documentKey) {
        logger.info("Starting GoogleOcr " + documentKey);
        Document document = new Document();
        document.setKey(documentKey);
        document.setPages(extractPages(imagePaths, documentKey));
        return document;
    }

    @Override
    public List<com.dsv.datafactory.model.Page> extractPages(List<String> imagePaths, String documentKey) throws IOException {
        ArrayList<com.dsv.datafactory.model.Page> pages = new ArrayList<>();
        List<Image> images = generateImages(imagePaths);
        List<AnnotateImageRequest> requests = generateRequests(images);
        for (int i = 0; i < imagePaths.size(); i++) {
            pages.add(extractPage(imagePaths.get(i), i, documentKey + i));
        }
        return pages;
    }

    private com.dsv.datafactory.model.Page extractPage(String imagePath, int pageNumber, String pageKey) {
        try {
            logger.info("Processing page: " + pageKey);
            AnnotateImageResponse rawResponse = generateResponse(imagePath);
            if (rawResponse == null) return null;

            GoogleVisionResponse response = new OcrParser(rawResponse).parse();
            GooglePage googlePage = response.getFullTextAnnotation().getPages().get(0);

            com.dsv.datafactory.model.Page page = new Page(pageNumber, pageKey, googlePage.getWidth(), googlePage.getHeight());
            page.setLanguage(languageOrchestrator.retrieveLanguagesFromPage(googlePage));
            page.setLines(lineOrchestrator.generateLines(retrieveAnnotationsList(response)));
            page.setRotation(wordOrchestrator.getPageRotation(page.getLines().get(0).getWords()));

            if (page.getRotation() != 0) wordOrchestrator.correctPageCoordinates(page);
            return page;
        } catch (Exception e) {
            logger.error("Error extracting OCR: " + e.getMessage(), e);
            return null;
        }
    }

    public ArrayList<Image> generateImages(List<String> imagePaths) throws IOException {
        return imagePaths.stream()
                .map(imageOrchestrator::processImage)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public AnnotateImageResponse generateResponse(String pathToImg) throws IOException {
        logger.info("Sending request to Google Vision for image: " + pathToImg);
        Image processedImage = imageOrchestrator.processImage(pathToImg); // Wynięść co się da do klas odpowiedzialnych za to
        List<AnnotateImageRequest> requests = imageOrchestrator.generatePngRequest(processedImage, this.feature); // Wynięść co się da do klas odpowiedzialnych za to
        AnnotateImageResponse response = imageOrchestrator.retrieveAnnotatedImageResponse(requests);
        if (response == null) {
            logger.error("Failed to retrieve response from Google Vision API");
            return null;
        } else if (!response.hasFullTextAnnotation()) {
            logger.error("Image {} contains no text", pathToImg);
            return null;
        } else if (response.hasError()) {
            logger.error("Error in Vision API call: {}", response.getError().getMessage());
            return null;
        }
        return response;
    }

    public List<com.dsv.datafactory.file.extraction.processor.models.EntityAnnotation> retrieveAnnotationsList(
            GoogleVisionResponse imageResponse) {
        return imageResponse.getTextAnnotations();
    }


}
