package com.dsv.datafactory.file.extraction.processor.models;

import lombok.*;

import java.util.ArrayList;

@Data
public class GooglePage {
    private int width;
    private int height;
    private ArrayList<GoogleBlock> blocks;
    private double confidence;
    private TextProperty textProperty;

}
