package com.loucans.bob.csvtojson.processor;

import com.loucans.bob.csvtojson.model.CsvRow;

import java.io.Closeable;

public interface OutputWriter extends Closeable {
    void writeRecord(CsvRow csvRow);
}
