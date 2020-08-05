package com.loucans.bob.csvtojson.parser;

import java.io.Closeable;

public interface CsvParser extends Closeable {
    void parse(CsvRowCallbackHandler rowCallbackHandler,
               CsvRowCallbackErrorHandler rowCallbackErrorHandler);
}
