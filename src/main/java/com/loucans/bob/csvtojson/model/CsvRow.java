package com.loucans.bob.csvtojson.model;

import static java.util.Arrays.copyOf;

// - --------------------------------------------------
// - simple dto for transferring csv rows to callbacks
// - FLUP - could this be the place to add validations and json serialization?
// - --------------------------------------------------
public class CsvRow {
    private final String[] rowData;

    public CsvRow(String[] rowData) {
        this.rowData = copyOf(rowData, rowData.length);
    }

    public String[] getRowData() {
        return copyOf(rowData, rowData.length);
    }
}
