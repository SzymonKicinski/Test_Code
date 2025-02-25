package com.dsv.datafactory.file.extraction.processor.models;

import lombok.Data;

import java.util.ArrayList;
@Data
public class TextAnnotation {
    private ArrayList<GooglePage> pages;
    private String text;
}

