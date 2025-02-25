package com.dsv.datafactory.file.extraction.processor.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
@Data
// Albo Same Setter / Getter ?
public class GoogleParagraph {
    private TextProperty property;
    private BoundingPoly boundingBox;
    private ArrayList<GoogleWord> words;
    private double confidence;
}
