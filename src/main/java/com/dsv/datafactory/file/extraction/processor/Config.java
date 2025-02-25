package com.dsv.datafactory.file.extraction.processor;

import com.google.cloud.vision.v1.Feature;
import lombok.Getter;
import lombok.Setter;

// #TODO Przeniesienie klasy Config do bardziej odpowiedniego pakietu może poprawić organizację projektu,
//  zwiększyć jego przejrzystość i ułatwić zarządzanie konfiguracją. Warto również rozważyć, czy klasa
//  Config powinna być bardziej modularna, na przykład poprzez podział na mniejsze klasy konfiguracyjne,
//  jeśli ma wiele różnych odpowiedzialności.
// Pakiet com.dsv.datafactory.file.extraction.processor.config: Możesz utworzyć nowy podpakiet config w processor,
// aby umieścić tam klasę Config. To wyraźnie wskazuje, że klasa ta jest odpowiedzialna za konfigurację.
//
// Pakiet com.dsv.datafactory.file.extraction.processor.common: Jeśli klasa Config jest używana w różnych
// kontekstach w całym projekcie, możesz rozważyć utworzenie pakietu common, który będzie zawierał klasy wspólne dla różnych komponentów.
// Zalety przeniesienia klasy Config
//Przejrzystość: Umieszczenie klasy Config w dedykowanym pakiecie ułatwia zrozumienie struktury projektu.
// Inni programiści będą mogli łatwo znaleźć klasę konfiguracyjną, co zwiększa czytelność kodu.
//
// Modularność: Jeśli klasa Config jest używana w różnych częściach projektu, umieszczenie jej w bardziej
// centralnym miejscu ułatwia zarządzanie i modyfikację konfiguracji bez konieczności przeszukiwania wielu pakietów.
//
//Testowanie: Klasa konfiguracyjna może być łatwiej testowana, jeśli jest umieszczona w odpowiednim pakiecie.
// Możesz również rozważyć dodanie testów jednostkowych dla tej klasy, aby upewnić się, że wszystkie
// właściwości są poprawnie ustawione.
// Może warto dodać adnotacje Data/Setter + getter i pola prywatne?

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
