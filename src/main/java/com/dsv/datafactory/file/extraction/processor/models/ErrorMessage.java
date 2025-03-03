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

    // If this class is to be sent over the network or saved to
// the database, it is worth using String for the error message itself instead of Throwable. -> serialization (may) be problematic
    @NonNull
    private Throwable exception;
}