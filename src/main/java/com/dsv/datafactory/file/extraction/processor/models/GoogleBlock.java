package com.dsv.datafactory.file.extraction.processor.models;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;


@Setter
@Getter
public class GoogleBlock {
    private TextProperty property;
    private BoundingPoly boundingBox;
    private ArrayList<GoogleParagraph> paragraphs;
    private String blockType;
    private double confidence;

}
