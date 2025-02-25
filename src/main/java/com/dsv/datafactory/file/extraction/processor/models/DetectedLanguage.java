package com.dsv.datafactory.file.extraction.processor.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DetectedLanguage {
    private float confidence;
    private String languageCode;

}
