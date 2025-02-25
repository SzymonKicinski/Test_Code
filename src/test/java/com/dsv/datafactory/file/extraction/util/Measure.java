/*
 * Copyright (c)
 * Author: Szymon Kiciński
 */

package com.dsv.datafactory.file.extraction.util;

import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcr;
import com.google.cloud.vision.v1.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Measure {

    private GoogleOcr ocr = new GoogleOcr();


    private Image processImage(File img) {
        try {
            return ocr.processImg(img.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ArrayList<String> getImagePaths(Path directory) {
        File[] files = directory.toFile().listFiles();
        if (files == null) {
            return new ArrayList<>(); // Zwraca pustą ArrayList, jeśli directory jest puste lub nie jest katalogiem
        }
        return Arrays.stream(files)
                .flatMap(file -> Arrays.stream(file.listFiles()).map(File::getAbsolutePath))
                .collect(Collectors.toCollection(ArrayList::new));
    }


    public long measureProcessingTime(Runnable task) {
        long startTime = System.currentTimeMillis();
        task.run();
        return System.currentTimeMillis() - startTime;
    }

    public int countWordsInDocument(ArrayList<String> sortedImageArray) {
        try {
            Document document = ocr.generateDocument(sortedImageArray, "large_file");
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return document.getPages().stream()
                .mapToInt(page -> page.getLines().stream().mapToInt(line -> line.getWords().size()).sum())
                .sum();
    }

    public void printAverages(int totalFiles, int totalWords, long totalProcessingTime, long totalJenksTime) {
        System.out.println("Average processing time: " + (totalProcessingTime / totalFiles));
        System.out.println("Average Jenks processing time: " + (totalJenksTime / totalFiles));
        System.out.println("Average words per file: " + (totalWords / totalFiles));
    }
}
