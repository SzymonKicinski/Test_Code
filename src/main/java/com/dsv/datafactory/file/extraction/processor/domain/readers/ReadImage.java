package com.dsv.datafactory.file.extraction.processor.domain.readers;

import com.dsv.datafactory.file.extraction.processor.domain.ExtractLines;
import com.dsv.datafactory.file.extraction.processor.domain.ocr.GoogleOcrP;
import com.dsv.datafactory.model.Document;

import javax.inject.Inject;

import java.util.ArrayList;

//Adnotacje springowe?
// Testy: Kod jest w miarę testowalny, ale jeśli googleOcrP.generateDocument() zwraca null,
// lineExtractor.generateInputFromDocument(document) może rzucić NullPointerException.
// Warto dodać zabezpieczenie przed tym
public class ReadImage {

    // Dodanie final
    private final GoogleOcrP googleOcrP;
    private final ExtractLines lineExtractor;

    @Inject
    public ReadImage(GoogleOcrP ocr, ExtractLines extractLines) {
        this.googleOcrP = ocr;
        this.lineExtractor = extractLines;
    }

    // Może użyć List -> Kod bardziej elastyczny?
    // Lepsza obsługa wyjątku -> throws OcrProcessingException, ExtractionException - customowe exception
    public Document extract(ArrayList<String> listOfPathImgs, String key) throws Exception {
        try {
            Document document = googleOcrP.generateDocument(listOfPathImgs, key);
            // Obsługa document == null ? Trój-argumentowy operator logiczny
            lineExtractor.generateInputFromDocument(document);
            // Dodać obsłgę Optionalli?
            // return Optional.of(document);
            return document;
        } catch (Exception e) {
            // Obłsuga wyjątku -> OcrProcessingException, ExtractionException
            // Dodać obsłgę Optionalli?
            //  return Optional.empty();
            return null;
        }

    }


}
