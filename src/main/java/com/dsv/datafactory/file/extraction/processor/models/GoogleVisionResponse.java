package com.dsv.datafactory.file.extraction.processor.models;

import lombok.Data;

import java.util.ArrayList;

@Data
public class GoogleVisionResponse {
    private ArrayList<EntityAnnotation> textAnnotations = new ArrayList<>();
    private TextAnnotation fullTextAnnotation;

}
