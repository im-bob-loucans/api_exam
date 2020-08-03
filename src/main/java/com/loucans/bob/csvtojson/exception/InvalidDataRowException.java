package com.loucans.bob.csvtojson.exception;

import com.loucans.bob.csvtojson.model.CsvRow;

import java.util.Arrays;

import static java.util.Optional.ofNullable;

// - --------------------------------------------------
// - error with an entire row of data
// - callbacks should throw this when validating rows of data
// - --------------------------------------------------
public class InvalidDataRowException extends CsvToJsonException {
    private final CsvRow csvRow;

    public InvalidDataRowException(String message, CsvRow csvRow) {
        super(message);
        this.csvRow = csvRow;
    }

    @Override
    public String getValue() {
        return Arrays.toString(ofNullable(csvRow).map(r -> r.getRowData()).orElse(null));
    }
}
