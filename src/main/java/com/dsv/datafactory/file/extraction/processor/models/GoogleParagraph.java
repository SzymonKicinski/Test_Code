package com.dsv.datafactory.file.extraction.processor.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
@Data
// Or only @Setter / @Getter ?
// @No/AllArgsConstructor??
public class GoogleParagraph {
    private TextProperty property;
    private BoundingPoly boundingBox;
    private ArrayList<GoogleWord> words;
    private double confidence;
}
