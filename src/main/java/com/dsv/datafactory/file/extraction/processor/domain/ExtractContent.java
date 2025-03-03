package com.dsv.datafactory.file.extraction.processor.domain;

import com.dsv.datafactory.file.extraction.processor.domain.readers.ReadImage;
import com.dsv.datafactory.file.extraction.processor.domain.saving.DocumentToDisk;
import com.dsv.datafactory.file.extraction.processor.logging.ECSLoggerProvider;
import com.dsv.datafactory.model.Document;
import com.dsv.datafactory.model.MetaData;
import com.dsv.logger.ECSLogger;

import javax.inject.Inject;
// Exception handling
// SOLID + Clean Code - use wherever possible!
// static in loggers -> Maybe some class who has predefined start of messages??
public class ExtractContent {
    private static final ECSLogger logger = ECSLoggerProvider.getLogger(ExtractContent.class.getName());

    private final ReadImage extractor;
    private final DocumentToDisk documentToDisk;

    @Inject
    public ExtractContent(ReadImage extractor, DocumentToDisk documentToDisk) {
        this.extractor = extractor;
        this.documentToDisk = documentToDisk;
    }

    public MetaData execute(MetaData metaData) {
        if (metaData == null) {
            logger.error("MetaData is null, cannot proceed.");
            return new MetaData();
        } else {
            logger.info("Starting content extraction for key: " + metaData.key);
            try {
                Document parsedDocument = extractor.extract(metaData.sortedImagePaths, metaData.key);

                logger.info("Saving HOCR document to disk...");
                documentToDisk.execute(metaData.key, parsedDocument, metaData.shipmentId);

                metaData.extractedOCRDocumentPath = parsedDocument.getPathToDocumentFile();
                logger.info("Extraction complete. MetaData paths set to: " + metaData.extractedOCRDocumentPath);
                return metaData;
            } catch (Exception e) {
                logger.error("Error during content extraction for key: " + metaData.key, e);
                e.printStackTrace();
                return new MetaData();
            }
        }
    }
}
