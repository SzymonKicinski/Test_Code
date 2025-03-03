package com.dsv.datafactory.file.extraction.processor.modules;

import com.google.cloud.vision.v1.Feature;
import com.dsv.datafactory.file.extraction.processor.Config;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import static com.dsv.datafactory.file.extraction.processor.util.ConfigurationLoader.getOrDefault;
import static com.dsv.datafactory.file.extraction.processor.util.ConfigurationLoader.getOrFail;

// #TODO I fixed some of it quickly but
// using constants for configuration keys
// Validating configuration values
// Using Enum for types
// Using a logger
// Refactoring the provideConfig method: You could consider
// splitting the provideConfig method into smaller methods to improve readability and make testing easier.
// Using Optional for configuration values
// Fixing a typo: There's a typo in the line where you set
// kakfaPollIntervalMs. It should be kafkaPollIntervalMs.
//
// + maybe it's worth moving some constants to some .properties? The string constants are rarely changed. (it's also worth considering
// values. What if they're changed, or they're different for different environments, then maybe @Profile from String would be better?

// #TODO - consider using Spring for this?
public class ConfigModule implements Module {

    // Constants for configuration keys
    private static final String KAFKA_CLIENT_ID = "KAFKA_CLIENT_ID";
    private static final String KAFKA_GROUP_ID = "KAFKA_GROUP_ID";
    private static final String ENABLE_KAFKA_SSL = "ENABLE_KAFKA_SSL";
    private static final String ENABLE_KAFKA_RBAC = "ENABLE_KAFKA_RBAC";
    private static final String RUN_GV_PARALLEL = "RUN_GV_PARALLEL";
    private static final String IMAGE_EXTRACTION_METADATA_TOPIC = "IMAGE_EXTRACTION_METADATA_TOPIC";
    private static final String GOOGLE_APPLICATION_CREDENTIALS = "GOOGLE_APPLICATION_CREDENTIALS";
    private static final String GOODNESS_OF_FIT = "GOODNESS_OF_FIT";
    private static final String START_CLASSES = "START_CLASSES";
    private static final String EXTRACTED_DOCUMENTS_TOPIC = "EXTRACTED_DOCUMENTS_TOPIC";
    private static final String JENKS_URI = "JENKS_URI";
    private static final String KAFKA_COMMIT_INTERVAL_MS = "KAFKA_COMMIT_INTERVAL_MS";
    private static final String KAFKA_POLL_INTERVAL_MS = "KAFKA_POLL_INTERVAL_MS";
    private static final String REQUEST_TIMEOUT_MS_CONFIG = "REQUEST_TIMEOUT_MS_CONFIG";
    private static final String KAFKA_AUTO_OFFSET_RESET = "KAFKA_AUTO_OFFSET_RESET";
    private static final String KAFKA_BOOTSTRAP_SERVERS = "KAFKA_BOOTSTRAP_SERVERS";
    private static final String KAFKA_MAX_REQUEST_SIZE = "KAFKA_MAX_REQUEST_SIZE";
    private static final String KAFKA_TRUSTSTORE_PATH = "KAFKA_TRUSTSTORE_PATH";
    private static final String KAFKA_TRUSTSTORE_FILE = "KAFKA_TRUSTSTORE_FILE";
    private static final String KAFKA_TRUSTSTORE_PASSWORD = "KAFKA_TRUSTSTORE_PASSWORD";
    private static final String KAFKA_SSL_PROTOCOL = "KAFKA_SSL_PROTOCOL";
    private static final String KAFKA_SSL_CIPHER_SUITE = "KAFKA_SSL_CIPHER_SUITE";

    @Override
    public void configure(Binder binder) {
    }

    @Provides
    @Singleton
    public Config provideConfig() {
        Config config = new Config();

        config.setKafkaClientId(getOrFail(KAFKA_CLIENT_ID));
        config.setKafkaGroupId(getOrFail(KAFKA_GROUP_ID));
        config.setEnableKafkaSSL(getOrDefault(ENABLE_KAFKA_SSL, "true"));
        config.setEnableRBAC(getOrDefault(ENABLE_KAFKA_RBAC, "false"));
        config.setRunGVInParallel(getOrDefault(RUN_GV_PARALLEL, "false"));

        config.setImageExtractionMetadataTopic(getOrFail(IMAGE_EXTRACTION_METADATA_TOPIC));
        config.setGoogleCredPath(getOrDefault(GOOGLE_APPLICATION_CREDENTIALS, "credentials/google-cred.json"));
        config.setGoodnessOfFit(getOrDefault(GOODNESS_OF_FIT, "0.999"));
        config.setStartNumberOfClasses(getOrDefault(START_CLASSES, ""));

        config.setExtractedDocumentTopic(getOrFail(EXTRACTED_DOCUMENTS_TOPIC));
        config.setLineServiceUrl(getOrDefault(JENKS_URI, "http://aif-jenks:8005/jenks/clustering"));

        config.setKafkaCommitIntervalMs(Integer.parseInt(getOrDefault(KAFKA_COMMIT_INTERVAL_MS, "200")));
        config.setKafkaPollIntervalMs(Integer.parseInt(getOrDefault(KAFKA_POLL_INTERVAL_MS, "1800000")));
        config.setKafkaRequestTimeoutMs(Integer.parseInt(getOrDefault(REQUEST_TIMEOUT_MS_CONFIG, "60000")));
        config.setKafkaAutoOffsetReset(getOrDefault(KAFKA_AUTO_OFFSET_RESET, "latest"));

        config.setFeature(Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build());

        config.setKafkaBootstrapServers(getOrDefault(KAFKA_BOOTSTRAP_SERVERS, "localhost:9092"));
        config.setKafkaMaxRequestSize(getOrDefault(KAFKA_MAX_REQUEST_SIZE, "104857600"));

        config.setKafkaTruststorePath(getOrDefault(KAFKA_TRUSTSTORE_PATH, ""));
        config.setKafkaTruststoreFile(getOrDefault(KAFKA_TRUSTSTORE_FILE, ""));
        config.setKafkaTruststorePassword(getOrDefault(KAFKA_TRUSTSTORE_PASSWORD, "", true));
        config.setKafkaSSLProtocol(getOrDefault(KAFKA_SSL_PROTOCOL, "TLSv1.3,TLSv1.2"));
        config.setKafkaSSLCipher(getOrDefault(KAFKA_SSL_CIPHER_SUITE, "TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA"));

        // #TODO Adding DLT in Kafka? What is the leading Kafka architecture? Fire&forget,Idempotence,mix?
// #TODO I would use this syntax
// properties.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE); //see also delivery.timeout.ms
// I would set Kafka configs in a separate class so as not to mix queue configuration with
// access or application configuration
        return config;
    }
}
