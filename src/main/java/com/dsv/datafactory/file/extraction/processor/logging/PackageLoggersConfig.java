package com.dsv.datafactory.file.extraction.processor.logging;

import com.dsv.logger.ECSLogger;

import static com.dsv.datafactory.file.extraction.processor.util.ConfigurationLoader.getOrDefault;
// Default Level Warn in Kafka
// No error checking when reading configuration variables
// KAFKA_LOGGER_NAME and PDFBOX_LOGGER_NAME. - more unique logger names for given application areas
public class PackageLoggersConfig {

    public static void configure() {
        configurePackageLogger("org.apache.kafka", "KAFKA_LOG_LEVEL");
        configurePackageLogger("org.apache.pdfbox.pdmodel.font", "LOG_LEVEL");
    }
    // BrainStorm talk -> Could this approach be useful in the future?
    public static void configure(String packageName, String level) {
        configurePackageLogger("org.apache.kafka", "KAFKA_LOG_LEVEL");
        configurePackageLogger("org.apache.pdfbox.pdmodel.font", "LOG_LEVEL");
        configurePackageLogger(packageName, level);
    }

    private static void configurePackageLogger(String packageName, String configKey) {
        ECSLogger.Level logLevel;
        try {
            logLevel = ECSLogger.Level.valueOf(getOrDefault(configKey, ECSLogger.Level.WARN.name()));
            logger.setLevel(packageName, logLevel);
            logger.info("Package " + packageName + " log level set to " + logLevel.name());

        } catch (IllegalArgumentException e) {
            logLevel = ECSLogger.Level.WARN;
            logger.warn("Invalid log level provided for " + packageName + ", defaulting to WARN. Error: " + e.getMessage());
        }
    }
}
