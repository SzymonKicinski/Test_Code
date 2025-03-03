package com.dsv.datafactory.file.extraction.processor.models;

import lombok.Data;

@Data
public class GoogleSymbol {
    private TextProperty property;
    private BoundingPoly boundingBox;
    private String text;
    private double confidence;
}

