package com.dsv.datafactory.file.extraction;


import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.domain.ExtractContent;

import com.dsv.datafactory.model.MetaData;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

import com.sun.xml.internal.bind.v2.TODO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;

///// path names must used forward "/" when building in linux env

@Disabled // Disabled due to missing resource files
// #TODO  @Disabled wskazuje, że test jest wyłączony z powodu brakujących plików zasobów. To jest dobra praktyka,
//  aby nie uruchamiać testów, które nie mogą być wykonane. Warto jednak dodać komentarz wyjaśniający, dlaczego test
//  jest wyłączony, co może pomóc innym programistom w przyszłości. -> A skoro nie ma plików resources to warto zastanowić
//  się czy ten test ma sens i czy nie warto go usunąć?
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BuildTest {
    // Tests to run when building jar file- much is taken from UnpackExtractHOCR but with specific examples
    private ExtractContent extractContent;
    // #TODO Upewnij się, że zasoby testowe (np. pliki PDF) są dostępne w odpowiedniej lokalizacji, aby testy mogły
    //  być uruchamiane w przyszłości. Może warto dodać mechanizm do sprawdzania, czy pliki istnieją przed ich użyciem.
    // czy ta ścieżka wgl jest poprawne??
    private File standard =new File("src/test/resources/build_data/standard/pdf_files");
    private ArrayList<String> newFormat =  new ArrayList<>(Arrays.asList("sample_outputalice_5_pg0","sample_outputalice_5_pg1","sample_outputalice_5_pg2","sample_outputalice_5_pg3","sample_outputalice_5_pg4"));

    @BeforeAll
    void setup() {
        Module modules = Modules.combine(getTestConfigModule());
        Injector injector = Guice.createInjector(modules);
        extractContent = injector.getInstance(ExtractContent.class);
    }

    private Module getTestConfigModule() {
        return new AbstractModule() {
            @Override protected void configure() {
                Config config = new Config();
                bind(Config.class).toInstance(config);
            }
        };
    }


    @Test
    void basicExtractionTest() throws IOException {
        ///Basic test for build, ensure that sample pdfs go through and produce hocr result
        // #TODO warto dodać obsługę sytuacji, gdy katalog jest pusty lub nie istnieje, aby uniknąć potencjalnych wyjątków.
        File[] files = standard.listFiles();
        for (File file:files){
            MetaData imageExtractionMetadata = new MetaData();
            imageExtractionMetadata.fileName = file.getName();
            imageExtractionMetadata.sortedImagePaths =newFormat;
            MetaData extraction = extractContent.execute(imageExtractionMetadata);
            assertNotNull(extraction);}
        }

        // #TODO
        // Więcej asercji: Można rozważyć dodanie dodatkowych asercji, aby sprawdzić, czy wynik extraction zawiera
        // oczekiwane dane, a nie tylko, że nie jest null.
        //Testy parametrów: Można również rozważyć użycie testów parametrów,
        // aby przetestować różne przypadki użycia z różnymi plikami PDF.
        // #TODO Given When Then - może warto opisać by test był czytleniejszy???
        // Podsumowując, test jest dobrze napisany i zorganizowany, ale można go
        // jeszcze poprawić, dodając więcej asercji, lepszą obsługę błędów oraz dokumentację.
    }

