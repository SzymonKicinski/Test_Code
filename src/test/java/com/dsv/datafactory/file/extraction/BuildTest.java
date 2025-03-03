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

///// path names must be used forward "/" when building in linux env

@Disabled // Disabled due to missing resource files
// #TODO @Disabled indicates that the test is disabled due to missing resource files. It is good practice to
// not run tests that cannot be executed. It is worth adding a comment explaining why the test is disabled, which may help other developers in the future. -> And since there are no resources files, it is worth considering
// whether this test makes sense and whether it is not worth removing it?
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class BuildTest {
    // Tests to run when building jar file- much is taken from UnpackExtractHOCR but with specific examples
    private ExtractContent extractContent;
    // #TODO Make sure that test resources (e.g. PDF files) are available in the right location so that tests can
// be run in the future. Maybe it is worth adding a mechanism to check if files exist before using them.
// is this wgl path correct??
    private File standard = new File("src/test/resources/build_data/standard/pdf_files");
    private ArrayList<String> newFormat = new ArrayList<>(Arrays.asList("sample_outputalice_5_pg0", "sample_outputalice_5_pg1", "sample_outputalice_5_pg2", "sample_outputalice_5_pg3", "sample_outputalice_5_pg4"));

    @BeforeAll
    void setup() {
        Module modules = Modules.combine(getTestConfigModule());
        Injector injector = Guice.createInjector(modules);
        extractContent = injector.getInstance(ExtractContent.class);
    }

    private Module getTestConfigModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                Config config = new Config();
                bind(Config.class).toInstance(config);
            }
        };
    }

    @Test
    void basicExtractionTest() throws IOException {
///Basic test for build, ensure that sample pdfs go through and produce hocr result
// #TODO it's worth adding support for when the directory is empty or doesn't exist to avoid potential exceptions.
        File[] files = standard.listFiles();
        for (File file : files) {
            MetaData imageExtractionMetadata = new MetaData();
            imageExtractionMetadata.fileName = file.getName();
            imageExtractionMetadata.sortedImagePaths = newFormat;
            MetaData extraction = extractContent.execute(imageExtractionMetadata);
            assertNotNull(extraction);
        }
    }

// #TODO
// More assertions: You could consider adding additional assertions to check if the extraction result contains
// the expected data, not just that it is not null.
// Parameter tests: You could also consider using parameter tests,
// to test different use cases with different PDFs.
// #TODO Given When Then - maybe describe it to make the test more readable???
// In summary, the test is well written and organized, but it could be improved
// by adding more assertions, better error handling, and documentation.
}