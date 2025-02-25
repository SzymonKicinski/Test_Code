/*
 * Copyright (c)
 * Author: Szymon Kiciński
 */

package com.dsv.datafactory.file.extraction.processor.domain.ocr.orchestrator;

import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcr;
import com.dsv.datafactory.file.extraction.processor.logging.ECSLoggerProvider;
import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


// Stworzyć klasę która będzie odpowaidać za redudantny kod
// Wynieść co jest możliwe to klas odpowiedzialnych za kod
public class ImageOrchestrator {

    private final static ECSLogger logger = ECSLoggerProvider.getLogger(GoogleOcr.class.getName());


    public List<AnnotateImageRequest> generatePngRequest(Image image, Feature feature){
        List<AnnotateImageRequest> requests = new ArrayList<>();
        AnnotateImageRequest request =
                AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(image).build();
        requests.add(request);
        return requests;
    }


    // KISS, YAGNI! -> To be deleted! - używane tylko w testach więc w testach powinno to znajdować się
    public List<AnnotateImageRequest> bulkGeneratePngRequest(List<Image> images, Feature feature){
        return images.parallelStream().flatMap(x-> generatePngRequest(x,feature).stream()).collect(Collectors.toList());
    }


    public Image processImage(String pathToImage) {
        try {
            byte[] data = Files.readAllBytes(Paths.get(pathToImage));
            ByteString imgBytes = ByteString.copyFrom(data);
            return Image.newBuilder().setContent(imgBytes).build();
        } catch (IOException e) {
            logger.error("Error loading image from path: " + pathToImage, e);
            return null;
        }
    }

    public AnnotateImageResponse retrieveAnnotatedImageResponse(List<AnnotateImageRequest> requests) throws IOException {
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            return client.batchAnnotateImages(requests).getResponses(0);
        }
    }

    public List<AnnotateImageResponse> retrieveAnnotatedImageResponseAll(List<AnnotateImageRequest> requests) {
        try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
            return client.batchAnnotateImages(requests).getResponsesList();
        } catch (IOException e) {
            logger.warn("IOException occurred: " + e.getMessage());
        } catch (Exception e) {
            logger.warn("An unexpected error occurred: " + e.getMessage());
        }
        return null;
    }

    public Optional<AnnotateImageResponse> getAnnotatedImageResponse(AnnotateImageRequest request) {
        try {
            return Optional.of(this.retrieveAnnotatedImageResponse(Collections.singletonList(request)));
        } catch (IOException e) {
            logger.error("Error retrieving annotated image response", e);
            return Optional.empty();
        }
    }

    public List<AnnotateImageResponse> getAnnotatedImageResponseBatch(List<AnnotateImageRequest> requests) {
        try {
            return this.retrieveAnnotatedImageResponseAll(requests);
        } catch (IOException e) {
            logger.error("Error retrieving annotated image response batch", e);
            return Collections.emptyList();
        }
    }
}
