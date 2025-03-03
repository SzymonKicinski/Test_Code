package com.dsv.datafactory.file.extraction.processor.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DetectedBreak {
    private String type;
    private int typeValue;
    private boolean isPrefix;

}
