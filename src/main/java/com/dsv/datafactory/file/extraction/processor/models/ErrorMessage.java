package com.dsv.datafactory.file.extraction.processor.models;

import com.dsv.datafactory.model.MetaData;
import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorMessage implements Serializable
{
    @NonNull
    private String topicKey;

    @NonNull
    private MetaData topicMessage;

    // Jeśli ta klasa ma być przesyłana przez sieć lub zapisywana do
    // bazy, warto zamiast Throwable użyć String dla samego komunikatu błędu. -> (może) być problematyczna  serializacja
    @NonNull
    private Throwable exception;
}
