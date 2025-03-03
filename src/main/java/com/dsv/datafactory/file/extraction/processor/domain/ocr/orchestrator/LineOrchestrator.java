package com.dsv.datafactory.file.extraction.processor.domain.ocr.orchestrator;

import com.dsv.datafactory.file.extraction.processor.models.BoundingPoly;
import com.dsv.datafactory.file.extraction.processor.models.EntityAnnotation;
import com.dsv.datafactory.file.extraction.processor.models.Vertices;
import com.dsv.datafactory.model.Word;
import javafx.scene.shape.Line;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


// Maybe instead of Orchestrator called the Services??
// @Services?
public class LineOrchestrator {

    public List<Line> generateLines(List<EntityAnnotation> annotations) {
        if (annotations.isEmpty()) {
            return Collections.emptyList();
        }
        annotations.remove(0); // Remove the first annotation (typically irrelevant)
        List<Word> words = annotations.stream()
                .map(this::generateWord)
                .collect(Collectors.toList());
        Line line = new Line();
        line.setWords(words);
        return Collections.singletonList(line);
    }

    public Word generateWord(EntityAnnotation annotation) {
        String description = annotation.getDescription();
        BoundingPoly bounding = annotation.getBoundingPoly();
        int[] minMaxCoordinates = getMinMaxCoordinatesFromVertices(bounding);

        Word word = new Word();
        word.setWord(description);
        word.setTopLeftCorner(createWordVertice(bounding.getVertices().get(0), minMaxCoordinates));
        word.setTopRightCorner(createWordVertice(bounding.getVertices().get(1), minMaxCoordinates));
        word.setLowRightCorner(createWordVertice(bounding.getVertices().get(2), minMaxCoordinates));
        word.setLowLeftCorner(createWordVertice(bounding.getVertices().get(3), minMaxCoordinates));
        // Maybe add some flag for OCR vs OCRP?
        word.setxMean(calculateMean(word.getTopLeftCorner().getX(), word.getLowRightCorner().getX()));
        word.setyMean(calculateMean(word.getTopLeftCorner().getY(), word.getLowRightCorner().getY()));
        word.setBoundingBox(new BoundingBox(minMaxCoordinates[0], minMaxCoordinates[2], minMaxCoordinates[1], minMaxCoordinates[3]));
        word.setRotation(getWordRotation(word, minMaxCoordinates));
        return word;
    }

    public int[] getMinMaxCoordinatesFromVertices(BoundingPoly vertices) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

        for (Vertices vertex : vertices.getVertices()) {
            int x = vertex.getX();
            int y = vertex.getY();
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minY = Math.min(minY, y);
            maxY = Math.max(maxY, y);
        }

        return new int[]{minX, minY, maxX, maxY};
    }

    public Vertices createWordVertice(Vertices vertex, int[] minMaxCoordinates) {
        int meanX = (minMaxCoordinates[0] + minMaxCoordinates[2]) / 2;
        int meanY = (minMaxCoordinates[1] + minMaxCoordinates[3]) / 2;

        int resX = vertex.getX() >= meanX ? minMaxCoordinates[2] : minMaxCoordinates[0];
        int resY = vertex.getY() >= meanY ? minMaxCoordinates[3] : minMaxCoordinates[1];

        return new Vertices(resX, resY);
    }

    private int getWordRotation(Word word, int[] minMaxCoordinates) {
        int minX = minMaxCoordinates[0];
        int maxX = minMaxCoordinates[2];
        int minY = minMaxCoordinates[1];
        int maxY = minMaxCoordinates[3];

        if (word.getTopLeftCorner().getX() == minX && word.getTopLeftCorner().getY() == minY) return 0;
        else if (word.getTopLeftCorner().getX() == minX && word.getTopLeftCorner().getY() == maxY) return 270;
        else if (word.getTopLeftCorner().getX() == maxX && word.getTopLeftCorner().getY() == minY) return 90;
        else return 180;
    }

    private int calculateMean(int cord1, int cord2) {
        return (cord1 + cord2) / 2;
    }
}