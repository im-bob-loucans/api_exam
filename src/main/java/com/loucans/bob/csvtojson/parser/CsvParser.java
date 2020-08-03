package com.loucans.bob.csvtojson.parser;

import java.io.File;

public interface CsvParser {
    void parse(File fileReader,
               CsvRowCallbackHandler rowCallbackHandler,
               CsvRowCallbackErrorHandler rowCallbackErrorHandler);
}
