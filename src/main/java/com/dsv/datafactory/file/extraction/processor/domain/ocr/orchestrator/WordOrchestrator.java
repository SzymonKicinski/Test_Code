/*
 * Copyright (c)
 * Author: Szymon Kiciński
 */

package com.dsv.datafactory.file.extraction.processor.domain.ocr.orchestrator;

import com.google.cloud.vision.v1.Word;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WordOrchestrator {

    public int getPageRotation(List<Word> words) {
        return words.parallelStream()
                .collect(Collectors.groupingBy(word -> word.getRotation() / 90, Collectors.counting()))
                .entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(entry -> (int) entry.getKey() * 90)
                .orElse(0);
    }

    // Problem with importing com.dsv.datafactory.model.Page -> models were in a separate module?? Impossible to compile code
    public void correctPageCoordinates(com.dsv.datafactory.model.Page page) {
        if (page.getRotation() == 90 || page.getRotation() == 270) {
            int temp = page.getHeight();
            page.setHeight(page.getWidth());
            page.setWidth(temp);
        }
        page.getLines().get(0).getWords().forEach(word -> correctWordCoordinates(
                word,
                page.getRotation(),
                page.getHeight(),
                page.getWidth()));
    }

    private void correctWordCoordinates(Word word, int rotation, int height, int width) {
        int minX = word.getBoundingBox().getX1();
        int maxX = word.getBoundingBox().getX2();
        int minY = word.getBoundingBox().getY1();
        int maxY = word.getBoundingBox().getY2();
        int x1, x2, y1, y2;
        if (rotation == 90) {
            x1 = minY;
            x2 = maxY;
            y1 = height - maxX;
            y2 = height - minX;
        } else if (rotation == 180) {
            x1 = width - maxX;
            x2 = width - minX;
            y1 = height - maxY;
            y2 = height - minY;
        } else if (rotation == 270) {
            x1 = width - maxY;
            x2 = width - minY;
            y1 = minX;
            y2 = maxX;
        } else return;
        word.setTopLeftCorner(new Vertices(x1, y1));
        word.setTopRightCorner(new Vertices(x2, y1));
        word.setLowRightCorner(new Vertices(x2, y2));
        word.setLowLeftCorner(new Vertices(x1, y2));
        word.setxMean(caclulateMean(word.getTopLeftCorner().getX(), word.getLowRightCorner().getX()));
        word.setyMean(caclulateMean(word.getTopLeftCorner().getY(), word.getLowRightCorner().getY()));
        word.setBoundingBox(new BoundingBox(x1, x2, y1, y2));
        word.setRotation(word.getRotation() - rotation);
    }
}
