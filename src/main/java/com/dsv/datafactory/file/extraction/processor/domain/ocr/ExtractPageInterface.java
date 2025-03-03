/*
 * Copyright (c)
 * Author: Szymon Kiciński
 */

package com.dsv.datafactory.file.extraction.processor.domain.ocr;


import com.dsv.datafactory.model.Page;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import java.io.IOException;
import java.util.List;

public interface ExtractPageInterface {

    List<Page> extractPages(
            List<String> imagePaths,
            String documentKey) throws IOException;

    // Add extractPage also -> #TODO
}
