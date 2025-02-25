package com.dsv.datafactory.file.extraction.processor.models;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityAnnotation {

    // Może warto dodać Optional do zmiennych?
    // private Optional<String> locale = Optional.empty();
    // dużo boiler code'u ale mamy zabezpieczenie przed null'ami?
    private String locale;
    private String description;
    private double confidence;
    private BoundingPoly boundingPoly;

    // Rozważyć czy warto to dodać oraz jaki zakres???
    public void setConfidence(double confidence) {
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("Confidence must be between 0.0 and 1.0");
        }
        this.confidence = confidence;
    }
}
