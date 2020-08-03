package com.loucans.bob.csvtojson.parser;

import com.loucans.bob.csvtojson.exception.CsvToJsonException;

public interface CsvRowCallbackErrorHandler {
    void handleError(Integer rowNum, CsvToJsonException error);
}
