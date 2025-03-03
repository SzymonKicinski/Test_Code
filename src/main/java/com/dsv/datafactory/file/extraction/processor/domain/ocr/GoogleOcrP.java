package com.dsv.datafactory.file.extraction.processor.domain.ocr;

import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.orchestrator.*;
import com.dsv.datafactory.file.extraction.processor.logging.ECSLoggerProvider;
import com.dsv.datafactory.file.extraction.processor.models.GooglePage;
import com.dsv.datafactory.file.extraction.processor.models.GoogleVisionResponse;
import com.dsv.datafactory.model.*;
import com.dsv.datafactory.model.Word;
import com.dsv.logger.ECSLogger;

import com.google.cloud.vision.v1.*;
import com.google.common.collect.Lists;
import org.w3c.dom.Document;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

// Adding Optionals
// Adding try-resources-catch
// Adding streams
// + Additionally, I would separate the common functionality to another class.
// A lot of code redundancy
// E.g. bulkGeneratePngRequest, processImg, generatePngRequest
// Separate methods from words and from png to other classes
// Wonder if it wouldn't be better to make an interface?
// @No/AllArgConstructor
// Add Spring annotations (Autowired etc.).
public class GoogleOcrP implements ExtractPageInterface {
    private static final ECSLogger logger = ECSLoggerProvider.getLogger(GoogleOcr.class.getName());
    private final Feature feature = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
    private final boolean runGVInParallel;
    private final int gvThreshold = 15;
    private final ImageOrchestrator imageOrchestrator;
    private final LineOrchestrator lineOrchestrator;
    private final LanguageOrchestrator languageOrchestrator;
    private final WordOrchestrator wordOrchestrator;

    @Inject
    public GoogleOcrP(Config config) {
        this.runGVInParallel = Boolean.parseBoolean(config.getRunGVInParallel());
        this.imageOrchestrator = new ImageOrchestrator();
        this.lineOrchestrator = new LineOrchestrator();
        this.languageOrchestrator = new LanguageOrchestrator();
        this.wordOrchestrator = new WordOrchestrator();
    }

    public Document generateDocument(List<String> imagePaths, String documentKey) throws IOException {
        try {
            logger.info("Starting Google OCR for document: " + documentKey);
            List<com.dsv.datafactory.model.Page> pages = extractPages(imagePaths, documentKey);

            AtomicInteger pageCount = new AtomicInteger();
            pages.forEach(page -> {
                page.setPageKey(documentKey + pageCount.getAndIncrement());
                page.setPageNumber(pageCount.get() - 1);
            });

            return new Document(documentKey, pages.stream()
                    .filter(page -> page.getLanguage() != null)
                    .collect(Collectors.toList()));

        } catch (Exception e) {
            logger.error("Error generating document: " + e.getMessage(), e);
            throw new IOException("Error generating document", e);
        }
    }

    public List<AnnotateImageResponse> extractFullDocumentResponse(List<AnnotateImageRequest> requests) throws IOException {
        if (runGVInParallel) {
            logger.info("Running Google Vision calls in parallel");
            return requests.parallelStream()
                    .map(imageOrchestrator::getAnnotatedImageResponse)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } else if (requests.size() >= gvThreshold) {
            logger.info("Chunking requests to threshold size: " + gvThreshold);
            return Lists.partition(requests, gvThreshold).parallelStream()
                    .map(imageOrchestrator::getAnnotatedImageResponseBatch)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());
        } else {
            return imageOrchestrator.getAnnotatedImageResponseBatch(requests);
        }
    }

    @Override
    public List<com.dsv.datafactory.model.Page> extractPages(List<String> imagePaths, String documentKey) throws IOException {
        List<Image> images = generateImages(imagePaths);
        List<AnnotateImageRequest> requests = generateRequests(images);
        List<AnnotateImageResponse> responses = extractFullDocumentResponse(requests);
        return responses.stream()
                .map(this::extractPage)
                .collect(Collectors.toList());
    }

    private com.dsv.datafactory.model.Page extractPage(AnnotateImageResponse response) {
        com.dsv.datafactory.model.Page page = new com.dsv.datafactory.model.Page();
        if (response.getFullTextAnnotation().getPagesCount() > 0) {
            GoogleVisionResponse parsedResponse = new OcrParser(response).parse();
            GooglePage googlePage = parsedResponse.getFullTextAnnotation().getPages().get(0);
            page.setHeight(googlePage.getHeight());
            page.setWidth(googlePage.getWidth());
            page.setLanguage(languageOrchestrator.retrieveLanguagesFromPage(googlePage));
            page.setLines(lineOrchestrator.generateLines(parsedResponse.getTextAnnotations()));
            page.setRotation(wordOrchestrator.getPageRotation(page.getLines().get(0).getWords()));
            if (page.getRotation() != 0) {
                wordOrchestrator.correctPageCoordinates(page);
            }
        }
        return page;
    }

    public ArrayList<Image> generateImages(List<String> imagePaths) throws IOException {
        return imagePaths.stream()
                .map(imageOrchestrator::processImage)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private List<AnnotateImageRequest> generateRequests(List<Image> images) {
        return images.stream()
                .map(image -> AnnotateImageRequest
                        .newBuilder()
                        .addFeatures(feature)
                        .setImage(image).build())
                        .collect(Collectors.toList());
    }


}
