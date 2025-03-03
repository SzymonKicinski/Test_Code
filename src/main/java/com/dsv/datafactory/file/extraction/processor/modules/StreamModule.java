package com.dsv.datafactory.file.extraction.processor.modules;

import com.dsv.datafactory.file.extraction.processor.Config;
import com.dsv.datafactory.file.extraction.processor.ExtractionStream;
import com.dsv.datafactory.file.extraction.processor.util.ConfigurationLoader;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import org.apache.kafka.clients.ClientDnsLookup;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;

import javax.inject.Singleton;
import java.util.Properties;


// #TODO
// Refactor configuration code: extract to separate methods
// Use constants
// Use Builder pattern
// Use Enum for protocols: Instead of using strings to define protocols.
// Validate configuration

// #TODO Split methods so that
// Configuration and Properties for Consumer and Producer are separate - functions (maybe even in separate classes)
// #TODO I would consider using Spring for this
// @EnableKafka
// @Configuration
// @RequiredArgsConstructor
// @FieldDefaults(level = PRIVATE, makeFinal = true)
public class StreamModule implements Module {

	private static final String KAFKA_PROCESSING_GUARANTEE = "KAFKA_PROCESSING_GUARANTEE";
	private static final String KAFKA_ISOLATION_LEVEL = "KAFKA_ISOLATION_LEVEL";
	private static final String ENABLE_KAFKA_CLOUD = "ENABLE_KAFKA_CLOUD";
	private static final String KAFKA_TRANSACTION_TIMEOUT_MS = "KAFKA_TRANSACTION_TIMEOUT_MS";
	private static final String MAX_POLL_RECORDS_CONFIG = "MAX_POLL_RECORDS_CONFIG";

	@Override
	public void configure(Binder binder) {
	}

	@Provides
	@Singleton
	public KafkaStreams provideStream(Config config, ExtractionStream extractionStream) {
		final StreamsBuilder builder = new StreamsBuilder();
		extractionStream.createFrom(builder);
		final Topology topology = builder.build();
		Properties props = createKafkaProperties(config);
		return new KafkaStreams(topology, props);
	}

	private Properties createKafkaProperties(Config config) {
		Properties props = new Properties();
		props.put(StreamsConfig.APPLICATION_ID_CONFIG, config.getKafkaClientId());
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.getKafkaAutoOffsetReset());
		props.put(ConsumerConfig.GROUP_ID_CONFIG, config.getKafkaGroupId());
		props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, config.getKafkaCommitIntervalMs());
		props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, config.getKafkaBootstrapServers());
		props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, ConfigurationLoader.getOrDefault(KAFKA_PROCESSING_GUARANTEE, "at_least_once"));
		props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, config.getKafkaPollIntervalMs());
		props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, config.getKafkaRequestTimeoutMs());
		props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, config.getKafkaMaxRequestSize());
		props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, config.getKafkaMaxRequestSize());
		props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, config.getKafkaMaxRequestSize());
		props.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, config.getKafkaMaxRequestSize());
		props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, ConfigurationLoader.getOrDefault(KAFKA_ISOLATION_LEVEL, "read_committed"));

		if (isKafkaCloudEnabled()) {
			configureKafkaCloud(props, config);
		} else if (config.getEnableKafkaSSL().equals("true")) {
			configureKafkaSSL(props, config);
		}

		return props;
	}

	private boolean isKafkaCloudEnabled() {
		return ConfigurationLoader.getOrDefault(ENABLE_KAFKA_CLOUD, "true").equals("true");
	}

	private void configureKafkaCloud(Properties props, Config config) {

		props.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, ConfigurationLoader.getOrDefault(KAFKA_TRANSACTION_TIMEOUT_MS, "600000"));
		props.put(ConsumerConfig.CLIENT_DNS_LOOKUP_CONFIG, ClientDnsLookup.USE_ALL_DNS_IPS.toString());
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
		props.put(SaslConfigs.SASL_JAAS_CONFIG, String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username='%s' password='%s';",
				ConfigurationLoader.getOrDefault("KAFKA_RBAC_USER", "", true),
				ConfigurationLoader.getOrDefault("KAFKA_RBAC_PW", "", true)));

// Calling the configureSSL method with the appropriate parameters
		configureSSL(props, config);
	}

	private void configureKafkaSSL(Properties props, Config config) {
		props.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, ConfigurationLoader.getOrDefault(KAFKA_TRANSACTION_TIMEOUT_MS, "600000"));
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, ConfigurationLoader.getOrDefault(MAX_POLL_RECORDS_CONFIG, "1"));
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, config.getEnableRBAC().equals("true") ? "SASL_SSL" : "SSL");
		if (config.getEnableRBAC().equals("true")) {
			props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
			props.put(SaslConfigs.SASL_JAAS_CONFIG, String.format("org.apache.kafka.common.security.plain.PlainLoginModule required username=\"%s\" password=\"%s\";",
					ConfigurationLoader.getOrDefault("KAFKA_RBAC_USER", "", true),
					ConfigurationLoader.getOrDefault("KAFKA_RBAC_PW", "", true)));
		}
		configureSSL(props, config);
	}

	private void configureSSL(Properties props, Config config) {
		props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, config.getKafkaTruststorePath() + '/' + config.getKafkaTruststoreFile());
		props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, config.getKafkaTruststorePassword());
		props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, config.getKafkaTruststorePath() + '/' + config.getKafkaTruststoreFile());
		props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, config.getKafkaTruststorePassword());
		props.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, config.getKafkaSSLProtocol());
		props.put(SslConfigs.SSL_CIPHER_SUITES_CONFIG, config.getKafkaSSLCipher());
		props.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "");
	}

}
