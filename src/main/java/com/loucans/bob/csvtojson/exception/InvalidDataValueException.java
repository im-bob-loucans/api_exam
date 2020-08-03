package com.loucans.bob.csvtojson.exception;

// - --------------------------------------------------
// - error with a value in row
// - callbacks should throw this when validating rows of data
// - --------------------------------------------------
public class InvalidDataValueException extends CsvToJsonException {
    private final String value;

    public InvalidDataValueException(String message, String value) {
        super(message);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }
}
