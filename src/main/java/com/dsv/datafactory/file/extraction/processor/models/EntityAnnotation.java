package com.dsv.datafactory.file.extraction.processor.models;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntityAnnotation {

    // Maybe it's worth adding Optional to variables?
// private Optional<String> locale = Optional.empty();
// a lot of boiler code but we have protection against nulls?
    private String locale;
    private String description;
    private double confidence;
    private BoundingPoly boundingPoly;

    // Consider whether it's worth adding and what scope???
    public void setConfidence(double confidence) {
        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("Confidence must be between 0.0 and 1.0");
        }
        this.confidence = confidence;
    }
}