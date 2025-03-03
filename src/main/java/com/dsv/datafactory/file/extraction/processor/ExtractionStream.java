package com.dsv.datafactory.file.extraction.processor;
import com.dsv.datafactory.model.MetaData;
import com.dsv.datafactory.serde.MetaDataSerde;
import com.google.inject.Inject;
import com.dsv.datafactory.file.extraction.processor.domain.*;
import org.apache.kafka.common.serialization.Serdes;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.KeyValue;

// #TODO adding a string, e.g. Autowirde AllArgsConstructor

// Collection types: Generic plus +
//Error handling:
//Use of final: It is worth marking class fields as final if they are not modified after construction,
// which increases code safety and readability.
// #TODO Adding Spring + Lombok?
public class ExtractionStream {

	private final Config config;
	private final ExtractContent extractDocument;

	@Inject
	public ExtractionStream(Config config, ExtractContent extractDocument) {
		this.config = config;
		this.extractDocument = extractDocument;
	}

	public void createFrom(StreamsBuilder builder) {
		KStream<String, MetaData> stream = builder.stream(
				config.getImageExtractionMetadataTopic(),
				Consumed.with(Serdes.String(), new MetaDataSerde())
		);
		// #TODO -> try & catch - maybe this would be better?
		KStream<String, MetaData> documentExtractions = stream.mapValues(extractDocument::execute);
		KStream<String, MetaData> documentExtractionsFiltered = documentExtractions.filter((k, v) -> v != null);

		documentExtractionsFiltered.peek((key, value) -> {
			if (value == null) {
				log.error("Null value for key: " + key);
			}
		});
		// some throws in this ExtractedDocumentTopic??
		documentExtractionsFiltered.to(config.getExtractedDocumentTopic(), Produced.with(Serdes.String(), new MetaDataSerde()));
	}
}
