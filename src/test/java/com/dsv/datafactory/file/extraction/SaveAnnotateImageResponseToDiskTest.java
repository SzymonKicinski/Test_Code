package com.dsv.datafactory.file.extraction;

import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcrP;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.orchestrator.ImageOrchestrator;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.orchestrator.LanguageOrchestrator;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.orchestrator.LineOrchestrator;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.orchestrator.WordOrchestrator;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SaveAnnotateImageResponseToDiskTest {
    private GoogleOcrP refac;
    private final String imageDir = Paths.get("src/test/resources/images/SerializationTest/").toAbsolutePath().toString();
    private final String dstDir = Paths.get("src/test/resources/AnnotateImageResponseObjects_v2/").toAbsolutePath().toString() + "/";
    ImageOrchestrator imageOrchestrator;

    @BeforeAll
    void setup() {
        Config config = new Config();
        config.setRunGVInParallel("false");
        refac = new GoogleOcrP(config);
        this.imageOrchestrator = new ImageOrchestrator();
    }

    @Test
    void processFilesAndSaveToDisk() throws IOException {
        File directory = new File(imageDir);
        File[] files = directory.listFiles();
        assertNotNull(files, "Katalog obrazów jest pusty lub nie istnieje");

        List<String> pathImages = List.of(files).stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());

        assertFalse(pathImages.isEmpty(), "Brak plików do przetworzenia");

        List<Image> images = refac.generateImages(pathImages);
        List<AnnotateImageRequest> requests = imageOrchestrator.bulkGeneratePngRequest(
                images, Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build()
        );

        List<AnnotateImageResponse> responses = refac.extractFullDocumentResponse(requests);
        assertEquals(pathImages.size(), responses.size(), "Nie wszystkie obrazy zostały przetworzone poprawnie");

        for (int i = 0; i < pathImages.size(); i++) {
            File image = new File(pathImages.get(i));
            String dst = dstDir + image.getName().replace(".png", ".object");

            try (FileOutputStream fileOutputStream = new FileOutputStream(dst);
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {

                objectOutputStream.writeObject(responses.get(i));
            } catch (IOException e) {
                throw new RuntimeException("Błąd zapisu pliku: " + dst, e);
            }

            assertTrue(new File(dst).exists(), "Plik nie został poprawnie zapisany: " + dst);
        }
    }

}