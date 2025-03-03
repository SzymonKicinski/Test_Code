package com.dsv.datafactory.file.extraction.processor.domain.saving;

import com.dsv.datafactory.file.extraction.processor.util.ConfigurationLoader;
import com.dsv.datafactory.model.Document;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;


import java.io.*;
import java.nio.charset.StandardCharsets;

// Spring annotation
// Maybe some application.properties storing constants e.g. endpoints??
public class DocumentToDisk {

    private static final String FILE_EXTENSION = ".json";
    private static final String PATH_DOCUMENT = "PATH_DOCUMENT";
    private static final String SLASH_FILES_SLASH = "/files/";
    private static final String HOCR_SLASH = "hocr/";

    final static Logger logger = Logger.getLogger(DocumentToDisk.class.getSimpleName());

    public void execute(String key, Document res,String shipmentId) throws IOException
    {
        String pathDocument =  ConfigurationLoader.getOrDefault(
                PATH_DOCUMENT,
                SLASH_FILES_SLASH + HOCR_SLASH) + shipmentId+"/";
        if (checkIfExists(pathDocument)) writeToJson(key, res, pathDocument);
    }

    // mkdir() will only create a directory if the parent directory already exists. // If shipmentId adds a new subfolder, the operation may fail.
    private boolean checkIfExists(String pathShipmentIdFolder) {
        File file = new File(pathShipmentIdFolder);
        if (!file.exists() && !file.mkdirs()) {
            logger.error("Failed to create directories: " + pathShipmentIdFolder);
        }
        return file.exists();
    }

    private void writeToJson(String key, Document doc, String pathDocument) throws IOException {
        String fullPath = pathDocument + key + FILE_EXTENSION;
        if (doc == null) {
            logger.error("Document is null, skipping JSON writing.");
            return;
        }
        doc.setPathToDocumentFile(fullPath);
        logger.info("fullpath is: " + fullPath);

        File file = new File(fullPath);
        // OutputStreamWriter (try-with-resources)
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            ObjectMapper mapper = new ObjectMapper();
            writer.write(mapper.writeValueAsString(doc));
        } catch (IOException e) {
            logger.error("Error while writing JSON file: " + fullPath, e);
            throw e;
        }
    }


}