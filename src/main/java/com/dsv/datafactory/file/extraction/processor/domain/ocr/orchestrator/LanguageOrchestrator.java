/*
 * Copyright (c)
 * Author: Szymon Kiciński
 */

package com.dsv.datafactory.file.extraction.processor.domain.ocr.orchestrator;

import com.dsv.datafactory.file.extraction.processor.models.GooglePage;

import java.util.List;
import java.util.stream.Collectors;

public class LanguageOrchestrator {

    public List<Language> retrieveLanguagesFromPage(GooglePage googlePage) {
        return googlePage.getTextProperty().getDetectedLanguages().stream()
                .map(language -> new Language(language.getConfidence(), language.getLanguageCode()))
                .collect(Collectors.toList());
    }
}
