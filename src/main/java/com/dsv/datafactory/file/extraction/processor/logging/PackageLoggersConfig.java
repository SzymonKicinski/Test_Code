package com.dsv.datafactory.file.extraction.processor.logging;

import com.dsv.logger.ECSLogger;

import static com.dsv.datafactory.file.extraction.processor.util.ConfigurationLoader.getOrDefault;
// Default Level Warn in Kafka
// Brak sprawdzania błędów przy odczycie zmiennych konfiguracyjnych
// KAFKA_LOGGER_NAME i PDFBOX_LOGGER_NAME. - bradziej unikalne nazwy logerów dladanych obszarów aplikacji
public class PackageLoggersConfig {

    public static void configure() {
        configurePackageLogger("org.apache.kafka", "KAFKA_LOG_LEVEL");
        configurePackageLogger("org.apache.pdfbox.pdmodel.font", "LOG_LEVEL");
    }

    // BrainStorm talk -> Czy takie podejście może byż użyteczne w późniejszej przyszłości?
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
