package com.dsv.datafactory.file.extraction.processor;

import com.google.cloud.vision.v1.Feature;
import lombok.Getter;
import lombok.Setter;

// #TODO Moving the Config class to a more appropriate package can improve the organization of the project,
// make it clearer and easier to manage configuration. It is also worth considering whether the
// Config class should be more modular, for example by splitting it into smaller configuration classes,
// if it has many different responsibilities.
// Package com.dsv.datafactory.file.extraction.processor.config: You can create a new subpackage config in processor,
// to put the Config class there. This clearly indicates that this class is responsible for configuration.
//
// Package com.dsv.datafactory.file.extraction.processor.common: If the Config class is used in different
// contexts throughout the project, you may want to consider creating a common package that contains classes that are common to different components.
// Advantages of moving the Config class
// Clarity: Putting the Config class in a dedicated package makes it easier to understand the structure of the project. // Other developers will be able to easily find the Config class, which makes the code more readable.
//
// Modularity: If the Config class is used in different parts of the project, placing it in a more
// central location makes it easier to manage and modify the configuration without having to search through multiple packages.
//
//Testing: The Config class can be more easily tested if it is placed in the right package.
// You may also want to consider adding unit tests for the class to make sure that all
// properties are set correctly.
// Maybe add Data/Setter + getter annotations and private fields?

@Getter
@Setter
public class Config {
	private String imageExtractionMetadataTopic;
	private Feature feature;
	private String extractedDocumentTopic;
	private String kafkaClientId;
	private String kafkaGroupId;
	private String kafkaAutoOffsetReset;
	private int kafkaCommitIntervalMs;
	private String enableKafkaSSL;
	private String kafkaBootstrapServers;
	private String kafkaMaxRequestSize;
	private String kafkaTruststorePath;
	private String kafkaTruststoreFile;
	private String kafkaTruststorePassword;
	private String kafkaSSLProtocol;
	private String kafkaSSLCipher;
	private int kafkaPollIntervalMs;
	private int kafkaRequestTimeoutMs;
	private String lineServiceUrl;
	private String startNumberOfClasses;
	private String enableRBAC;
	private String goodnessOfFit;
	private String googleCredPath;
	private String runGVInParallel;

}
