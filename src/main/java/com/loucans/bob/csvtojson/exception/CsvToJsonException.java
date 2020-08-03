package com.loucans.bob.csvtojson.exception;

// - --------------------------------------------------
// - base exception for bubbling errors out of callbacks
// - --------------------------------------------------
public abstract class CsvToJsonException extends RuntimeException {

    public CsvToJsonException(String message) {
        super(message);
    }

    public abstract String getValue();
}
