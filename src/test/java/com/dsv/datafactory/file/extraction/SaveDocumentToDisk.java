package com.dsv.datafactory.file.extraction;

import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcr;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.OcrParser;
import com.dsv.datafactory.file.extraction.processor.models.GoogleVisionResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class SaveDocumentToDisk {
    private final GoogleOcr googleOcr = new GoogleOcr();
    private final String imageDir = "src/test/resources/images/SerializationTest/";
    private final String airDir = "src/test/resources/AnnotateImageResponseObjects/";
    private final String dstDir = "src/test/resources/SerializedGoogleVisionResponses/";

    @Test
    void processAnnotateImageResponseAndSaveToDisk() {
        File[] files = new File(airDir).listFiles();
        assertNotNull(files, "Pliki w katalogu " + airDir + " nie istnieją lub nie można ich odczytać");

        for (File image : files) {
            try {
                String imagePath = image.getAbsolutePath();
                String dst = dstDir + image.getName().replace(".object", ".json");
                AnnotateImageResponse response = loadAnnotateImageResponseFromDisk(imagePath);
                assertNotNull(response, "Błąd: response jest null dla " + imagePath);

                GoogleVisionResponse parsed = new OcrParser(response).parse();
                assertNotNull(parsed, "Błąd: parsed jest null dla " + imagePath);

                saveDocument(parsed, dst);
                assertTrue(new File(dst).exists(), "Plik wynikowy nie został utworzony: " + dst);
            } catch (Exception e) {
                throw new RuntimeException("Błąd podczas przetwarzania pliku: " + image.getName(), e);
            }
        }
    }

    @Test
    void processImagesAndSaveToDisk() {
        File[] files = new File(imageDir).listFiles();
        assertNotNull(files, "Pliki w katalogu " + imageDir + " nie istnieją lub nie można ich odczytać");

        for (File image : files) {
            try {
                String imagePath = image.getAbsolutePath();
                String dst = dstDir + image.getName().replace(".png", ".json");
                AnnotateImageResponse response = googleOcr.generateResponse(imagePath);
                assertNotNull(response, "Błąd: response jest null dla " + imagePath);

                GoogleVisionResponse parsed = new OcrParser(response).parse();
                assertNotNull(parsed, "Błąd: parsed jest null dla " + imagePath);

                saveDocument(parsed, dst);
                assertTrue(new File(dst).exists(), "Plik wynikowy nie został utworzony: " + dst);
            } catch (Exception e) {
                throw new RuntimeException("Błąd podczas przetwarzania pliku: " + image.getName(), e);
            }
        }
    }

    private String serialize(GoogleVisionResponse doc) {
        try {
            return new ObjectMapper().writeValueAsString(doc);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Błąd podczas serializacji dokumentu", e);
        }
    }

    private void saveDocument(GoogleVisionResponse document, String dst) {
        String serialized = serialize(document);
        if (serialized != null) {
            try (FileOutputStream outputStream = new FileOutputStream(dst)) {
                outputStream.write(serialized.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException("Błąd zapisu dokumentu do pliku: " + dst, e);
            }
        }
    }

    private AnnotateImageResponse loadAnnotateImageResponseFromDisk(String objPath) {
        try (FileInputStream fileInputStream = new FileInputStream(objPath);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            return (AnnotateImageResponse) objectInputStream.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas wczytywania obiektu AnnotateImageResponse z: " + objPath, e);
        }
    }

    private GoogleVisionResponse loadGoogleVisionResponseFromDisk(String path) {
        try (FileInputStream fileInputStream = new FileInputStream(path)) {
            String sResponse = IOUtils.toString(fileInputStream, StandardCharsets.UTF_8);
            return new ObjectMapper().readValue(sResponse, GoogleVisionResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas wczytywania obiektu GoogleVisionResponse z: " + path, e);
        }
    }
}
