package com.loucans.bob.csvtojson.parser;

import com.loucans.bob.csvtojson.exception.CsvToJsonException;
import com.loucans.bob.csvtojson.model.CsvRow;

public interface CsvRowCallbackHandler {
    void handleRow(Integer rowNum, CsvRow csvRow) throws CsvToJsonException;
}
