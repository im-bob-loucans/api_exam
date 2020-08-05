package com.loucans.bob.csvtojson;

import com.loucans.bob.csvtojson.exception.InvalidDataRowException;
import com.loucans.bob.csvtojson.processor.CsvProcessor;
import org.json.JSONArray;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CsvToJsonIntegrationTests {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private CsvProcessor service;
    private File inputDir;
    private File outputDir;
    private File errorDir;

    @Before
    public void setup() throws IOException {
        inputDir = folder.newFolder("input");
        outputDir = folder.newFolder("output");
        errorDir = folder.newFolder("error");
        service = new CsvProcessor();
    }

    @After
    public void teardown() {
        inputDir.delete();
        outputDir.delete();
        errorDir.delete();
    }

    /**
     * use case - valid csv file with records that are "well-formed"
     * - verify - data is copied over without change
     * - verify - valid json structure and data types
     * - verify - valid json
     */
    @Test
    public void happyPath() throws IOException {
        String inputFileName = "/happy_path.csv";
        String outputFileName = "/happy_path.json";
        Files.copy(
                Path.of(csvFile(inputFileName).getAbsolutePath()),
                Path.of(inputDir.getAbsolutePath() + inputFileName));

        service.processCsvFile(
                inputFileName,
                inputDir.getAbsolutePath(),
                outputDir.getAbsolutePath(),
                errorDir.getAbsolutePath());

        // no original file exists
        assertFalse(
                Files.exists(Path.of(inputDir.getAbsolutePath() + inputFileName)));

        // outputs json data matching input csv data
        String json =
                Files.readString(
                        Path.of(outputDir.getAbsolutePath() + outputFileName));
        assertEquals(
                "[\n" +
                "{\"phone\":\"555-555-5550\",\"name\":{\"middle\":\"middle_name_0\",\"last\":\"last_name_0\",\"first\":\"first_name_0\"},\"id\":12345670},\n" +
                "{\"phone\":\"555-555-5551\",\"name\":{\"middle\":\"middle_name_1\",\"last\":\"last_name_1\",\"first\":\"first_name_1\"},\"id\":12345671}\n" +
                "]",
                json);
        assertEquals(2, new JSONArray(json).length());

        // no error file exists
        assertFalse(
                Files.exists(Path.of(errorDir.getAbsolutePath() + inputFileName)));
    }

    /**
     * use case - empty csv file
     * - verify valid empty array json is produced
     * - verify no errors written
     */
    @Test
    public void emptyFile() throws IOException {
        String inputFileName = "/empty_file.csv";
        String outputFileName = "/empty_file.json";
        Files.copy(
                Path.of(csvFile(inputFileName).getAbsolutePath()),
                Path.of(inputDir.getAbsolutePath() + inputFileName));

        service.processCsvFile(
                inputFileName,
                inputDir.getAbsolutePath(),
                outputDir.getAbsolutePath(),
                errorDir.getAbsolutePath());

        // no original file exists
        assertFalse(
                Files.exists(Path.of(inputDir.getAbsolutePath() + inputFileName)));

        // no json file exists
        assertFalse(
                Files.exists(Path.of(outputDir.getAbsolutePath() + outputFileName)));

        // error file contains empty file error at line 0
        String errors =
                Files.readString(
                        Path.of(errorDir.getAbsolutePath() + inputFileName));

        assertEquals(
                "\"LINE_NUM\",\"ERROR_MSG\"\r\n" +
                "\"0\",\"\"empty file\"\"\r\n", errors);
    }

    /**
     * use case - no rows csv file
     * - verify valid empty array json is produced
     * - verify no errors written
     */
    @Test
    public void headerOnly() throws IOException {
        String inputFileName = "/headers_only.csv";
        String outputFileName = "/headers_only.json";
        Files.copy(
                Path.of(csvFile(inputFileName).getAbsolutePath()),
                Path.of(inputDir.getAbsolutePath() + inputFileName));

        service.processCsvFile(
                inputFileName,
                inputDir.getAbsolutePath(),
                outputDir.getAbsolutePath(),
                errorDir.getAbsolutePath());

        // no original file exists
        assertFalse(
                Files.exists(Path.of(inputDir.getAbsolutePath() + inputFileName)));

        // outputs empty json data
        String json =
                Files.readString(
                        Path.of(outputDir.getAbsolutePath() + outputFileName));
        assertEquals("[]", json);
        assertEquals(0, new JSONArray(json).length());

        // no error file exists
        assertFalse(
                Files.exists(Path.of(errorDir.getAbsolutePath() + inputFileName)));
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
    public void emptyValuesInRows() throws IOException {
        String inputFileName = "/empty_values.csv";
        String outputFileName = "/empty_values.json";
        Files.copy(
                Path.of(csvFile(inputFileName).getAbsolutePath()),
                Path.of(inputDir.getAbsolutePath() + inputFileName));

        service.processCsvFile(
                inputFileName,
                inputDir.getAbsolutePath(),
                outputDir.getAbsolutePath(),
                errorDir.getAbsolutePath());

        // no original file exists
        assertFalse(
                Files.exists(Path.of(inputDir.getAbsolutePath() + inputFileName)));

        // data contains 1 records without middle name
        String json =
                Files.readString(
                        Path.of(outputDir.getAbsolutePath() + outputFileName));

        assertEquals(
                "[\n" +
                "{\"phone\":\"555-555-5552\",\"name\":{\"last\":\"last_name_2\",\"first\":\"first_name_2\"},\"id\":12345672}\n" +
                "]",
                json);
        assertEquals(1, new JSONArray(json).length());

        // error file contains records for the failed rows
        String errors =
                Files.readString(
                        Path.of(errorDir.getAbsolutePath() + inputFileName));

        assertEquals(
                "\"LINE_NUM\",\"ERROR_MSG\"\r\n" +
                "\"2\",\"invalid INTERNAL_ID [required, length <= 8, digits only]\"\r\n" +
                "\"3\",\"invalid FIRST_NAME [required, length <= 15]\"\r\n" +
                "\"5\",\"invalid LAST_NAME [required, length <= 15]\"\r\n" +
                "\"6\",\"invalid PHONE_NUM [required, length = 12, format: ###-###-####]\"\r\n",
                errors);
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
    public void invalidValues() throws IOException {
        String inputFileName = "/invalid_values.csv";
        String outputFileName = "/invalid_values.json";
        Files.copy(
                Path.of(csvFile(inputFileName).getAbsolutePath()),
                Path.of(inputDir.getAbsolutePath() + inputFileName));

        service.processCsvFile(
                inputFileName,
                inputDir.getAbsolutePath(),
                outputDir.getAbsolutePath(),
                errorDir.getAbsolutePath());

        // no original file exists
        assertFalse(
                Files.exists(Path.of(inputDir.getAbsolutePath() + inputFileName)));

        // json is valid and contains 0 records
        String json =
                Files.readString(
                        Path.of(outputDir.getAbsolutePath() + outputFileName));
        assertEquals("[]", json);
        assertEquals(0, new JSONArray(json).length());

        // error file contains records for the failed rows
        String errors =
                Files.readString(
                        Path.of(errorDir.getAbsolutePath() + inputFileName));

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
                errors);
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
    public void spacesForValues() throws IOException {
        String inputFileName = "/spaces_for_values.csv";
        String outputFileName = "/spaces_for_values.json";
        Files.copy(
                Path.of(csvFile(inputFileName).getAbsolutePath()),
                Path.of(inputDir.getAbsolutePath() + inputFileName));

        service.processCsvFile(
                inputFileName,
                inputDir.getAbsolutePath(),
                outputDir.getAbsolutePath(),
                errorDir.getAbsolutePath());

        // no original file exists
        assertFalse(
                Files.exists(Path.of(inputDir.getAbsolutePath() + inputFileName)));

        // json is valid and contains 3 records with the blank values
        String json =
                Files.readString(
                        Path.of(outputDir.getAbsolutePath() + outputFileName));
        assertEquals(
                "[\n" +
                "{\"phone\":\"555-555-5556\",\"name\":{\"middle\":\"middle_name_6\",\"last\":\"last_name_6\",\"first\":\" \"},\"id\":12345676},\n" +
                "{\"phone\":\"555-555-5556\",\"name\":{\"middle\":\" \",\"last\":\"last_name_6\",\"first\":\"first_name_6\"},\"id\":12345676},\n" +
                "{\"phone\":\"555-555-5556\",\"name\":{\"middle\":\"middle_name_6\",\"last\":\" \",\"first\":\"first_name_6\"},\"id\":12345676}\n" +
                "]",
                json);
        assertEquals(3, new JSONArray(json).length());

        // error file contains records for the failed rows
        String errors =
                Files.readString(
                        Path.of(errorDir.getAbsolutePath() + inputFileName));
        assertEquals(
                "\"LINE_NUM\",\"ERROR_MSG\"\r\n" +
                "\"2\",\"invalid INTERNAL_ID [required, length <= 8, digits only]\"\r\n" +
                "\"6\",\"invalid PHONE_NUM [required, length = 12, format: ###-###-####]\"\r\n",
                errors);
    }

    /**
     * use case - files has invalid headers
     * - verify processing rejects and error is produced for variations of header errors
     */
    @Test
    public void invalidHeaders() throws IOException {
        for (int i = 1; i <=5; i++) {
            String inputFileName = "/invalid_headers_" + i + ".csv";
            String outputFileName = "invalid_headers_" + i + ".json";

            try {
                Files.copy(
                        Path.of(csvFile(inputFileName).getAbsolutePath()),
                        Path.of(inputDir.getAbsolutePath() + inputFileName));

                service.processCsvFile(
                        inputFileName,
                        inputDir.getAbsolutePath(),
                        outputDir.getAbsolutePath(),
                        errorDir.getAbsolutePath());

            } catch (InvalidDataRowException e) {
                // swallow
            }

            // no original file exists
            assertFalse(
                    Files.exists(Path.of(inputDir.getAbsolutePath() + inputFileName)));

            // no json file exists
            assertFalse(
                    Files.exists(Path.of(outputDir.getAbsolutePath() + outputFileName)));

            // error file contains records for the failed rows
            String errors =
                    Files.readString(
                            Path.of(errorDir.getAbsolutePath() + inputFileName));
            assertEquals(
                    "\"LINE_NUM\",\"ERROR_MSG\"\r\n" +
                    "\"1\",\"csv header row is null or does not match expected header definition\"\r\n",
                    errors);
        }
    }

    @Test
    public void misalignedQuotedValuesInHeaderRow() throws IOException {
        String inputFileName = "/malformed_quoted_value_2.csv";
        String outputFileName = "/malformed_quoted_value_2.json";
        Files.copy(
                Path.of(csvFile(inputFileName).getAbsolutePath()),
                Path.of(inputDir.getAbsolutePath() + inputFileName));

        try {
            service.processCsvFile(
                    inputFileName,
                    inputDir.getAbsolutePath(),
                    outputDir.getAbsolutePath(),
                    errorDir.getAbsolutePath());
        } catch (InvalidDataRowException e) {
            // swallow
        }

        assertFalse(
                Files.exists(Path.of(outputDir.getAbsolutePath() + outputFileName)));

        String errors =
                Files.readString(
                        Path.of(errorDir.getAbsolutePath() + inputFileName));
        assertEquals(
                "\"LINE_NUM\",\"ERROR_MSG\"\r\n" +
                "\"1\",\"(startline 1) EOF reached before encapsulated token finished\"\r\n",
                errors);
    }

    @Test
    public void misalignedQuotedValuesInDataRow() throws IOException {
        String inputFileName = "/malformed_quoted_value_1.csv";
        String outputFileName = "/malformed_quoted_value_1.json";
        Files.copy(
                Path.of(csvFile(inputFileName).getAbsolutePath()),
                Path.of(inputDir.getAbsolutePath() + inputFileName));

        try {
            service.processCsvFile(
                    inputFileName,
                    inputDir.getAbsolutePath(),
                    outputDir.getAbsolutePath(),
                    errorDir.getAbsolutePath());
        } catch (InvalidDataRowException e) {
            // swallow
        }

        // no original file exists
        assertFalse(
                Files.exists(Path.of(inputDir.getAbsolutePath() + inputFileName)));

        // json is valid and contains 0 records
        String json =
                Files.readString(
                        Path.of(outputDir.getAbsolutePath() + outputFileName));
        assertEquals("[]", json);
        assertEquals(0, new JSONArray(json).length());

        // error file contains records for the failed rows
        String errors =
                Files.readString(
                        Path.of(errorDir.getAbsolutePath() + inputFileName));
        assertEquals(
                "\"LINE_NUM\",\"ERROR_MSG\"\r\n" +
                 "\"2\",\"(startline 2) EOF reached before encapsulated token finished\"\r\n",
                errors);
    }

    private File csvFile(String testFileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        URL resource = classLoader.getResource(
                format("com/loucans/bob/csvtojson/csvs%s", testFileName));
        if (Objects.isNull(resource)) {
            throw new RuntimeException();
        }

        return new File(resource.getFile());
    }
}
