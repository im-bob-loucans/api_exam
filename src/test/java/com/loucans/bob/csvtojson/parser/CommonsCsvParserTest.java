package com.loucans.bob.csvtojson.parser;

import com.loucans.bob.csvtojson.model.CsvRow;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class CommonsCsvParserTest {
    private CommonsCsvParser parser;
    private Map<Integer, CsvRow> processedRows;
    private Map<Integer, Exception> errors;
    private CsvRowCallbackHandler rowHandler;
    private CsvRowCallbackErrorHandler errorHandler;

    @Before
    public void setup() {
        processedRows = new HashMap<>();
        errors = new HashMap<>();
        rowHandler = (rowNum, csvRow) -> processedRows.put(rowNum, csvRow);
        errorHandler = (rowNum, error) -> errors.put(rowNum, error);
    }

    @After
    public void teardown() throws IOException {
        if (nonNull(parser)) {
            parser.close();
        }
    }

    @Test
    public void parse_shouldProduceRows_whenFileIsWellFormed() {
        parser = new CommonsCsvParser(csvFile("happy_path"));

        parser.parse(rowHandler, errorHandler);

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
        parser = new CommonsCsvParser(csvFile("headers_only"));

        parser.parse(rowHandler, errorHandler);

        assertEquals(1, processedRows.size());
        assertHeader(processedRows.get(1).getRowData());
        assertEquals(0, errors.size());
    }

    @Test
    public void parse_shouldProduceRows_whenRowIsMissingSomeData() {
        parser = new CommonsCsvParser(csvFile("row_missing_data"));

        parser.parse(rowHandler, errorHandler);

        assertEquals(2, processedRows.size());
        assertHeader(processedRows.get(1).getRowData());
        String[] emptyRow = processedRows.get(2).getRowData();
        assertArrayEquals(new String[]{"a1", "b1"}, emptyRow);
        assertEquals(0, errors.size());
    }

    @Test
    public void parse_shouldProduceErrors_whenCsvHasMalformedQuotedData() {
        parser = new CommonsCsvParser(csvFile("malformed_quoted_value"));

        parser.parse(rowHandler, errorHandler);

        assertEquals(1, processedRows.size());
        assertHeader(processedRows.get(1).getRowData());
        assertEquals(1, errors.size());
        assertEquals("(startline 2) EOF reached before encapsulated token finished", errors.get(2).getMessage());
    }

    @Test
    public void parse_shouldProduceNothing_whenFileIsEmpty() {
        parser = new CommonsCsvParser(csvFile("empty_file"));

        parser.parse(rowHandler, errorHandler);

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
