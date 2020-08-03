package com.loucans.bob.csvtojson.parser;

import com.loucans.bob.csvtojson.model.CsvRow;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CommonsCsvRfc4180ProcessorTest {

    private CommonsCsvParser parser;
    private Map<Integer, CsvRow> processedRows;
    private Map<Integer, Exception> errors;

    private CsvRowCallbackHandler rowCallbackHandler;
    private CsvRowCallbackErrorHandler rowCallbackErrorHandler;

    @Before
    public void setup() {
        parser = new CommonsCsvParser();
        processedRows = new HashMap<>();
        errors = new HashMap<>();
        rowCallbackHandler = (rowNum, csvRow) -> processedRows.put(rowNum, csvRow);
        rowCallbackErrorHandler = (rowNum, error) -> errors.put(rowNum, error);
    }

    @Test
    public void parse_shouldProduceRows_whenFileIsWellFormed() {
        parser.parse(
                csvFile("happy_path"), rowCallbackHandler, rowCallbackErrorHandler);

        assertEquals(2, processedRows.size());
        assertHeader(processedRows.get(1).getRowData());
        String[] row1 = processedRows.get(2).getRowData();
        assertArrayEquals(
                new String[]{"a1", "b1", "c1", "d1", "e1"},
                row1);
        assertEquals(0, errors.size());
    }

    @Test
    public void parse_shouldProduceRows_whenFileContainsOnlyHeaders() {
        parser.parse(
                csvFile("headers_only"), rowCallbackHandler, rowCallbackErrorHandler);

        assertEquals(1, processedRows.size());
        assertHeader(processedRows.get(1).getRowData());
        assertEquals(0, errors.size());
    }

    @Test
    public void parse_shouldProduceRows_whenRowIsMissingSomeData() {
        parser.parse(
                csvFile("row_missing_data"), rowCallbackHandler, rowCallbackErrorHandler);

        assertEquals(2, processedRows.size());
        assertHeader(processedRows.get(1).getRowData());
        String[] emptyRow = processedRows.get(2).getRowData();
        assertArrayEquals(new String[]{"a1", "b1"}, emptyRow);
        assertEquals(0, errors.size());
    }

    @Test
    public void parse_shouldProduceErrors_whenCsvHasMalformedQuotedData() {
        parser.parse(
                csvFile("malformed_quoted_value"), rowCallbackHandler, rowCallbackErrorHandler);

        assertEquals(1, processedRows.size());
        assertHeader(processedRows.get(1).getRowData());
        assertEquals(1, errors.size());
        assertEquals("(startline 2) EOF reached before encapsulated token finished", errors.get(2).getMessage());
    }

    @Test
    public void parse_shouldProduceNothing_whenFileIsEmpty() {
        parser.parse(
                csvFile("empty_file"), rowCallbackHandler, rowCallbackErrorHandler);

        assertEquals(0, processedRows.size());
        assertEquals(0, errors.size());
    }

    private File csvFile(String testFileName) {
        ClassLoader classLoader = getClass().getClassLoader();

        URL resource = classLoader.getResource(
                format("com/loucans/bob/csvtojson/csvs/%s.csv", testFileName));
        if (Objects.isNull(resource)) {
            throw new RuntimeException();
        }

        return new File(resource.getFile());
    }

    private void assertHeader(String[] headers) {
        assertArrayEquals(
                new String[]{"a", "b", "c", "d", "e"},
                headers);
    }
}
