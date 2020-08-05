package com.loucans.bob.csvtojson;

import com.loucans.bob.csvtojson.exception.InvalidDataRowException;
import com.loucans.bob.csvtojson.processor.CsvProcessor;
import com.loucans.bob.csvtojson.processor.ErrorLogger;
import com.loucans.bob.csvtojson.processor.OutputWriter;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.join;
import static org.junit.Assert.assertEquals;

public class CsvToJsonIntegrationTests {

    private CsvProcessor service;
    private OutputWriter mockOutput;
    private ErrorLogger mockError;
    private List<String> output;
    private List<String> errors;

    @Before
    public void setup() {
        service = new CsvProcessor();
        resetOuput();
        resetErrors();
    }

    /**
     * use case - valid csv file with records that are "well-formed"
     * - verify - data is copied over without change
     * - verify - valid json structure and data types
     * - verify - valid json
     */
    @Test
    public void happyPath() {
        service.processCsvFile(csvFile("happy_path"), mockOutput, mockError);

        // outputs json data matching input csv data
        assertEquals(
                "[\n" +
                "{\"phone\":\"555-555-5550\",\"name\":{\"middle\":\"middle_name_0\",\"last\":\"last_name_0\",\"first\":\"first_name_0\"},\"id\":12345670},\n" +
                "{\"phone\":\"555-555-5551\",\"name\":{\"middle\":\"middle_name_1\",\"last\":\"last_name_1\",\"first\":\"first_name_1\"},\"id\":12345671}\n" +
                "]",
                join(output, ""));

        // json is valid and contains two records
        assertEquals(2, new JSONArray(join(output, "")).length());
    }

    /**
     * use case - empty csv file
     * - verify valid empty array json is produced
     * - verify no errors written
     */
    @Test
    public void emptyFile() {
        service.processCsvFile(csvFile("empty_file"), mockOutput, mockError);

        // outputs empty json data
        assertEquals("", join(output, ""));

        // error file contains only headers
        assertEquals(
                "\"LINE_NUM\",\"ERROR_MSG\"\r\n" +
                "\"0\",\"\"empty file\"\"\r\n", join(errors, ""));
    }

    /**
     * use case - no rows csv file
     * - verify valid empty array json is produced
     * - verify no errors written
     */
    @Test
    public void headerOnly() {
        service.processCsvFile(csvFile("headers_only"), mockOutput, mockError);

        // outputs empty json data
        assertEquals("[]", join(output, ""));

        // json is valid and contains 0 records
        assertEquals(0, new JSONArray(join(output, "")).length());

        // error file contains only headers
        assertEquals("", join(errors, ""));
    }

    /**
     * use case - required data validation
     * - verify middle name is optional and is omitted from output json
     * - verify id required
     * - verify phone required
     * - verify first name required
     * - verify last name required
     * - verify errors are quoted comma delimited values
     * - verify row nums are logged
     * - verify human readable error messages
     */
    @Test
    public void emptyValuesInRows() {
        service.processCsvFile(csvFile("empty_values"), mockOutput, mockError);

        // data contains 1 records without middle name
        assertEquals(
                "[\n" +
                "{\"phone\":\"555-555-5552\",\"name\":{\"last\":\"last_name_2\",\"first\":\"first_name_2\"},\"id\":12345672}\n" +
                "]",
                join(output, ""));

        // json is valid and contains two records
        assertEquals(1, new JSONArray(join(output, "")).length());

        // error file contains records for the failed rows
        assertEquals(
                "\"LINE_NUM\",\"ERROR_MSG\"\r\n" +
                "\"2\",\"invalid INTERNAL_ID [required, length <= 8, digits only]\"\r\n" +
                "\"3\",\"invalid FIRST_NAME [required, length <= 15]\"\r\n" +
                "\"5\",\"invalid LAST_NAME [required, length <= 15]\"\r\n" +
                "\"6\",\"invalid PHONE_NUM [required, length = 12, format: ###-###-####]\"\r\n",
                join(errors, ""));
    }

    /**
     * use case - required data validation
     * - verify first name <= 15 chars
     * - verify middle name <= 15 chars
     * - verify last name <= 15 chars
     * - verify phone = 12 chars
     * - verify phone matches ###-###-#### format
     * - verify id <= 8 chars
     * - verify id digits only
     */
    @Test
    public void invalidValues() {
        service.processCsvFile(csvFile("invalid_values"), mockOutput, mockError);

        // json is valid and contains 0 records
        assertEquals("[]", join(output, ""));
        assertEquals(0, new JSONArray(join(output, "")).length());

        // error file contains records for the failed rows
        assertEquals(
                "\"LINE_NUM\",\"ERROR_MSG\"\r\n" +
                "\"2\",\"invalid INTERNAL_ID [required, length <= 8, digits only]\"\r\n" +
                "\"3\",\"invalid INTERNAL_ID [required, length <= 8, digits only]\"\r\n" +
                "\"4\",\"invalid INTERNAL_ID [required, length <= 8, digits only]\"\r\n" +
                "\"5\",\"invalid INTERNAL_ID [required, length <= 8, digits only]\"\r\n" +
                "\"6\",\"invalid FIRST_NAME [required, length <= 15]\"\r\n" +
                "\"7\",\"invalid MIDDLE_NAME [length <= 15]\"\r\n" +
                "\"8\",\"invalid LAST_NAME [required, length <= 15]\"\r\n" +
                "\"9\",\"invalid PHONE_NUM [required, length = 12, format: ###-###-####]\"\r\n" +
                "\"10\",\"invalid PHONE_NUM [required, length = 12, format: ###-###-####]\"\r\n" +
                "\"11\",\"invalid PHONE_NUM [required, length = 12, format: ###-###-####]\"\r\n" +
                "\"12\",\"invalid PHONE_NUM [required, length = 12, format: ###-###-####]\"\r\n" +
                "\"13\",\"invalid PHONE_NUM [required, length = 12, format: ###-###-####]\"\r\n" +
                "\"14\",\"invalid PHONE_NUM [required, length = 12, format: ###-###-####]\"\r\n" +
                "\"15\",\"invalid PHONE_NUM [required, length = 12, format: ###-###-####]\"\r\n" +
                "\"16\",\"invalid PHONE_NUM [required, length = 12, format: ###-###-####]\"\r\n" +
                "\"17\",\"invalid PHONE_NUM [required, length = 12, format: ###-###-####]\"\r\n",
                join(errors, ""));
    }

    /**
     * use case - verify spaced (empty strings are interpreted as values)
     * - verify char fields accept blank strings as a valid value
     * - verify first name processed
     * - verify middle name processed
     * - verify last name processed
     * - verify id rejects
     * - verify phone rejects
     */
    @Test
    public void spacesForValues() {
        service.processCsvFile(csvFile("spaces_for_values"), mockOutput, mockError);

        // json is valid and contains 3 records with the blank values
        assertEquals(
                "[\n" +
                "{\"phone\":\"555-555-5556\",\"name\":{\"middle\":\"middle_name_6\",\"last\":\"last_name_6\",\"first\":\" \"},\"id\":12345676},\n" +
                "{\"phone\":\"555-555-5556\",\"name\":{\"middle\":\" \",\"last\":\"last_name_6\",\"first\":\"first_name_6\"},\"id\":12345676},\n" +
                "{\"phone\":\"555-555-5556\",\"name\":{\"middle\":\"middle_name_6\",\"last\":\" \",\"first\":\"first_name_6\"},\"id\":12345676}\n" +
                "]",
                join(output, ""));
        assertEquals(3, new JSONArray(join(output, "")).length());

        // error file contains errors
        assertEquals(
                "\"LINE_NUM\",\"ERROR_MSG\"\r\n" +
                "\"2\",\"invalid INTERNAL_ID [required, length <= 8, digits only]\"\r\n" +
                "\"6\",\"invalid PHONE_NUM [required, length = 12, format: ###-###-####]\"\r\n",
                join(errors, ""));
    }

    /**
     * use case - files has invalid headers
     * - verify processing rejects and error is produced for variations of header errors
     */
    @Test
    public void invalidHeaders() {
        for (int i = 1; i <=5; i++) {
            try {
                setup();
                service.processCsvFile(csvFile("invalid_headers_" + i), mockOutput, mockError);
            } catch (InvalidDataRowException e) {
            }
            assertEquals("", join(output, ""));
            assertEquals(
                    "\"LINE_NUM\",\"ERROR_MSG\"\r\n" +
                    "\"1\",\"csv header row is null or does not match expected header definition\"\r\n",
                    join(errors, ""));
        }
    }

    @Test
    public void misalignedQuotedValues() {
        service.processCsvFile(csvFile("malformed_quoted_value_1"), mockOutput, mockError);
        assertEquals("[]", join(output, ""));
        assertEquals(
                "\"LINE_NUM\",\"ERROR_MSG\"\r\n" +
                "\"2\",\"(startline 2) EOF reached before encapsulated token finished\"\r\n",
                join(errors, ""));

        try {
            setup();
            service.processCsvFile(csvFile("malformed_quoted_value_2"), mockOutput, mockError);
        } catch (InvalidDataRowException e) {
        }
        assertEquals("", join(output, ""));
        assertEquals(
                "\"LINE_NUM\",\"ERROR_MSG\"\r\n" +
                "\"1\",\"(startline 1) EOF reached before encapsulated token finished\"\r\n",
                join(errors, ""));
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

    private void resetOuput() {
        output = new ArrayList<>();
        mockOutput = toWrite -> output.add(toWrite);
    }

    private void resetErrors() {
        errors = new ArrayList<>();
        // some extra "stuff" here cause I want the tests to assert what
        // i expect but the processing needs to be cleaned up
        AtomicBoolean fileCreated = new AtomicBoolean(false);
        mockError = (rowNum, errorToLog) -> {
            if (!fileCreated.get()) {
                errors.add("\"LINE_NUM\",\"ERROR_MSG\"\r\n");
                fileCreated.set(true);
            }
            errors.add(String.format("\"%d\",\"%s\"\r\n", rowNum, errorToLog));
        };
    }
}
