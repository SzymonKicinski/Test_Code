package com.dsv.datafactory.file.extraction.processor.domain.readers;

import com.dsv.datafactory.file.extraction.processor.domain.ExtractLines;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcrP;
import com.dsv.datafactory.model.Document;

import javax.inject.Inject;

import java.util.ArrayList;

// Spring annotations?
// Tests: The code is fairly testable, but if googleOcrP.generateDocument() returns null,
// lineExtractor.generateInputFromDocument(document) may throw a NullPointerException.
// It's worth adding a safeguard against this
public class ReadImage {

    // Adding final
    private final GoogleOcrP googleOcrP;
    private final ExtractLines lineExtractor;

    @Inject
    public ReadImage(GoogleOcrP ocr, ExtractLines extractLines) {
        this.googleOcrP = ocr;
        this.lineExtractor = extractLines;
    }

    // Maybe use List -> More flexible code?
    // Better exception handling -> throws OcrProcessingException, ExtractionException - custom exception
    public Document extract(ArrayList<String> listOfPathImgs, String key) throws Exception {
        try {
            Document document = googleOcrP.generateDocument(listOfPathImgs, key);
// Handling document == null ? Three-argument logical operator
            lineExtractor.generateInputFromDocument(document);
// Add Optional support?
// return Optional.of(document);
            return document;
        } catch (Exception e) {
// Exception handling -> OcrProcessingException, ExtractionException
// Add Optional support?
// return Optional.empty();
            return null;
        }

    }


}
