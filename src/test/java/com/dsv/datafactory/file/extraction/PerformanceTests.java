package com.dsv.datafactory.file.extraction;

import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.domain.ExtractContent;
import com.dsv.datafactory.file.extraction.processor.domain.ExtractLines;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcr;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcrP;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.orchestrator.ImageOrchestrator;
import com.dsv.datafactory.file.extraction.util.Measure;
import com.dsv.datafactory.model.Document;
import com.dsv.datafactory.model.MetaData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


// przeniesiono metody do klasy Measure
// Jeśli nie używamy plików z resourceów to warto je usunąć
//      private Path mid = Paths.get("src","test","resources","performance","mid/");
//      private Path small = Paths.get("src","test","resources","performance","small/");
// Przydało by się w testach coś mierzyć jakieś wartości, które powiedzą czy testy
// wydajnościowe przechodzi aplikacja
// ++ jeśli tylko w testach używamy bulkGeneratePngRequest to może
// warto to wynieść do testów,a nie trzymać w głównym kodzie
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Disabled
public class PerformanceTests {

    private GoogleOcr ocr;
    private Measure measure;
    public GoogleOcrP refac;
    private ImageOrchestrator imageOrchestrator;
    private Config config;
    private ExtractLines extractLines;
    private ExtractContent extractContent;

    private Path large = Paths.get("src", "test", "resources", "performance", "large/");
    private ObjectMapper mapper;

    @BeforeAll
    void setup() {
        ocr = new GoogleOcr();
        measure = new Measure();
        imageOrchestrator = new ImageOrchestrator();
        config = new Config();
        extractLines = new ExtractLines(config);
        extractContent = createInjector().getInstance(ExtractContent.class);
        mapper = new ObjectMapper();
    }

    private Injector createInjector() {
        Module modules = Modules.combine(getTestConfigModule());
        return Guice.createInjector(modules);
    }

    private Module getTestConfigModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Config.class).toInstance(config);
                config.lineServiceUrl = "http://localhost:8005/jenks/clustering";
                config.goodnessOfFit = ".999";
                config.startNumberOfClasses = "5";
                config.RunGVInParallel = "false";
            }
        };
    }

    @Test
    public void testLargeFiles() throws IOException {
        measure.measureProcessingTime(() -> {
            ArrayList<String> largeImgs = measure.getImagePaths(large);
            try {
                Document document = ocr.generateDocument(largeImgs, "large_file");
                extractLines.generateInputFromDocument(document);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Test
    public void testLargeFilesAverage() throws IOException {
        int totalFiles = 0;
        int totalWords = 0;
        long totalProcessingTime = 0;
        long totalJenksTime = 0;

        for (File file : large.toFile().listFiles()) {
            ArrayList<String> sortedImageArray = getImagePaths(file.toPath());
            totalFiles++;
            long processingTime = measure.measureProcessingTime(() -> {
                try {
                    ocr.generateDocument(sortedImageArray, "large_file");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            totalProcessingTime += processingTime;

            int numWords = countWordsInDocument(sortedImageArray);
            totalWords += numWords;

            long jenksTime = measure.measureProcessingTime(() -> extractLines.generateInputFromDocument(document));
            totalJenksTime += jenksTime;
        }
        measure.printAverages(totalFiles, totalWords, totalProcessingTime, totalJenksTime);
    }

    @Test
    public void testLargeFilesIsolatedGvision() throws IOException {
        processFilesInDirectory(large, file -> {
            for (File img : file.listFiles()) {
                measure.measureProcessingTime(() -> {
                    try {
                        Image processedImage = imageOrchestrator.processImg(img.getAbsolutePath());
                        List<AnnotateImageRequest> requests = imageOrchestrator.generatePngRequest(processedImage,
                                Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build());
                        imageOrchestrator.retrieveAnnotatedImageResponse(requests);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
    }

    @Test
    public void testLargeFilesIsolatedGvisionBulk() throws IOException {
        processFilesInDirectory(large, file -> {
            List<Image> images = Arrays.stream(file.listFiles())
                    .map(this::processImage)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            List<AnnotateImageRequest> requests = imageOrchestrator.bulkGeneratePngRequest(images,
                    Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build());

            measure.measureProcessingTime(() -> {
                try {
                    imageOrchestrator.retrieveAnnotatedImageResponseAll(requests);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        });
    }

    @Test
    public void testFileOverThresholdGV() throws IOException {
        List<String> pathImages = getImagePaths(Paths.get(large.toString(), "many_pages"));
        List<Image> images = refac.generateImages(pathImages);

        long processingTime = measure.measureProcessingTime(() -> imageOrchestrator.bulkGeneratePngRequest(images,
                Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build()));
        System.out.println("Processing time: " + processingTime);
    }

    @Test
    public void testEntireExtraction() {
        processFilesInDirectory(large, file -> {
            List<String> sortedImageArray = measure.getImagePaths(file.toPath());
            MetaData document = new MetaData();
            document.sortedImagePaths = sortedImageArray;
            document.key = file.getName();
            document.shipmentId = file.getName();

            long processingTime = measure.measureProcessingTime(() -> extractContent.execute(document));
            System.out.println("Average time taken for: " + file.getName() + " " + processingTime);
        });
    }

    private void processFilesInDirectory(Path directory, Consumer<File> fileProcessor) {
        for (File file : directory.toFile().listFiles()) {
            fileProcessor.accept(file);
        }
    }

    private Image processImage(File img) {
        try {
            return imageOrchestrator.processImg(img.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<String> getImagePaths(Path directory) {
        return Arrays.stream(directory.toFile().listFiles())
                .flatMap(file -> Arrays.stream(file.listFiles()).map(File::getAbsolutePath))
                .collect(Collectors.toCollection());
    }

    private int countWordsInDocument(ArrayList<String> sortedImageArray) {
        try {
            Document document = ocr.generateDocument(sortedImageArray, "large_file");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return document.getPages().stream()
                .mapToInt(page -> page.getLines().stream().mapToInt(line -> line.getWords().size()).sum())
                .sum();
    }
}