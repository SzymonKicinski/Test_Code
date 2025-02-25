package com.dsv.datafactory.file.extraction.processor.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextProperty {
    private ArrayList<DetectedLanguage> detectedLanguages;
    private DetectedBreak detectedBreak;
}
