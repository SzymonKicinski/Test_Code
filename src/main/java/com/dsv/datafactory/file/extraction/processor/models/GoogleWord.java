package com.dsv.datafactory.file.extraction.processor.models;

import lombok.Data;

import java.util.ArrayList;

@Data
public class GoogleWord {
    private TextProperty property;
    private BoundingPoly boundingBox;
    private ArrayList<GoogleSymbol> symbols;
    private double confidence;

}
