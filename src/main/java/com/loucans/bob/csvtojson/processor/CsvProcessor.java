package com.loucans.bob.csvtojson.processor;

import com.loucans.bob.csvtojson.exception.InvalidDataRowException;
import com.loucans.bob.csvtojson.exception.InvalidDataValueException;
import com.loucans.bob.csvtojson.model.CsvRow;
import com.loucans.bob.csvtojson.parser.CommonsCsvParser;
import com.loucans.bob.csvtojson.parser.CsvRowCallbackErrorHandler;
import com.loucans.bob.csvtojson.parser.CsvRowCallbackHandler;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

// - --------------------------------------------------
// - main logic for processing a file per requirements
// - FLUP - this class is need to be broken up and redesigned
//        - break up this callback approach and just process
//          file row by row would have been better
// - --------------------------------------------------
public class CsvProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvProcessor.class);
    private boolean headerValid = false;
    private int rowsProcessed = 0;

    // - --------------------------------------------------
    // - define the metadata about the expected input csv
    // - --------------------------------------------------
    // FLUP - refactor this
    private enum Fields {
        INTERNAL_ID(
                (value) -> {
                    if (isEmpty(value) || !value.matches("\\d{8}")) {
                        throw new InvalidDataValueException(
                                "invalid INTERNAL_ID [required, length <= 8, digits only]", value);
                    }
                }),

        FIRST_NAME(
                (value) -> {
                    if (isEmpty(value) || value.length() > 15) {
                        throw new InvalidDataValueException(
                                "invalid FIRST_NAME [required, length <= 15]", value);
                    }
                }),

        MIDDLE_NAME(
                (value) -> {
                    if (!isEmpty(value) && value.length() > 15) {
                        throw new InvalidDataValueException(
                                "invalid MIDDLE_NAME [length <= 15]", value);
                    }
                }),

        LAST_NAME(
                (value) -> {
                    if (isEmpty(value) || value.length() > 15) {
                        throw new InvalidDataValueException(
                                "invalid LAST_NAME [required, length <= 15]", value);
                    }
                }),

        PHONE_NUM(
                (value) -> {
                    if (isEmpty(value) || !(value.length() == 12) || !value.matches("\\d{3}-\\d{3}-\\d{4}")) {
                        throw new InvalidDataValueException(
                                "invalid PHONE_NUM [required, length = 12, format: ###-###-####]", value);
                    }
                });

        public final Consumer<String> validator;

        Fields(Consumer<String> validator) {
            this.validator = validator;
        }
    }

    public void processCsvFile(String csvFileName,
                               String inputPath,
                               String outputPath,
                               String errorPath) {

        File csvFile =
                new File(inputPath + "/" + csvFileName);

        Path outputFile =
                Paths.get(outputPath + "/" + csvFileName.replace(".csv", ".json"));
        try {
            Files.deleteIfExists(outputFile);
            Files.createFile(outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Error records should be written to a csv file in error-directory
        // the error file should match the name of the input file.
        // FLUP - if errors exist, one error file is created per input file. - need to create only when an error occurs
        // - an error record should contain:
        //   - LINE_NUM : the number of the record which was invalid
        //   - ERROR_MSG : a human readable error message about what validation failed
        // FLUP - in the event of name collision, the latest file should overwrite the earlier version.
        //   - collect errors
        Path errorFile =
                Paths.get(errorPath + "/" + csvFileName.replace(".csv", "errors.csv"));
        try {
            Files.deleteIfExists(errorFile);
            Files.createFile(errorFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        processCsvFile(csvFile,
                (rowJson) -> {
                    try {
                        Files.writeString(outputFile, rowJson, UTF_8, APPEND);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                },
                (errorMessage) -> {
                    try {
                        Files.writeString(errorFile, errorMessage, UTF_8, APPEND);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public void processCsvFile(File csvFile,
                               OutputWriter outputFile,
                               ErrorLogger erroLogger) {

        List<String> jsonRow = new ArrayList<>();
        CsvRowCallbackHandler rowHandler = (rowNum, csvRow) -> {
            if (isHeaderRow(rowNum)) {
                assertValidHeaderRow(csvRow);
                headerValid = true;
            } else {
                assertValidDataRow(csvRow);
                if (rowsProcessed == 0) {
                    outputFile.writeString("[" + lineSeparator());
                }

                if (jsonRow.size() > 0 && StringUtils.isNotEmpty(jsonRow.get(0))) {
                    outputFile.writeString(jsonRow.get(0) + "," + lineSeparator());
                    jsonRow.clear();
                }

                String json = rowtoJson(csvRow);
                jsonRow.add(json);
                rowsProcessed++;

                LOGGER.debug("processed csvRow to json: [{}]", json);
            }
        };

        erroLogger.logError("LINE_NUM,ERROR_MSG" + lineSeparator());
        CsvRowCallbackErrorHandler errorHandler = (rowNum, error) -> {
            String message = format(
                    "csv row failed: value: [%s], error: [%s]", error.getValue(), error.getMessage());

            erroLogger.logError(
                    format("\"%d\",\"%s\"", rowNum, message.replace("\"", "\"\"")) + lineSeparator());

            LOGGER.error(message);

            // FLUP - processing should continue in the event of an invalid row;
            //         all errors should be collected and added to the corresponding error csv.
            //         should this stop processing?
            if (isHeaderRow(rowNum)) {
                // abort for processing errors on header row
                throw error;
            }
        };

        new CommonsCsvParser()
                .parse(csvFile, rowHandler, errorHandler);

        if (headerValid == false) {
            erroLogger.logError(
                    format("\"%d\",\"empty file\"", 0));
        }

        if (jsonRow.size() > 0 && StringUtils.isNotEmpty(jsonRow.get(0))) {
            outputFile.writeString(jsonRow.get(0) + lineSeparator());
            jsonRow.clear();
        }

        if (rowsProcessed > 0) {
            outputFile.writeString("]");
        } else {
            outputFile.writeString("[]");
        }

        csvFile.delete();
    }

    private boolean isHeaderRow(Integer rowNum) {
        return rowNum == 1;
    }

    private void assertValidHeaderRow(CsvRow csvRow) {
        if (isNull(csvRow) || isNull(csvRow.getRowData()) ||
                Fields.values().length != csvRow.getRowData().length) {
            throw new InvalidDataRowException(
                    "csv header row is null or does not match expected header definition", csvRow);
        }

        for (int i = 0; i < Fields.values().length; i++) {
            if (!StringUtils.equals(Fields.values()[i].name(), csvRow.getRowData()[i])) {
                throw new InvalidDataRowException(
                        "csv header row is null or does not match expected header definition", csvRow);
            }
        }
    }

    private void assertValidDataRow(CsvRow csvRow) {
        if (isNull(csvRow) || isNull(csvRow.getRowData()) ||
                Fields.values().length != csvRow.getRowData().length) {
            throw new InvalidDataRowException(
                    "csv data row is null or does not match expected header definition", csvRow);
        }

        for (int i = 0; i < Fields.values().length; i++) {
            Fields.values()[i].validator.accept((csvRow.getRowData()[i]));
        }
    }

    private String rowtoJson(CsvRow csvRow) {
        String[] data = csvRow.getRowData();

        JSONObject name = new JSONObject();
        name.put("first", data[1]);
        if (isNotEmpty(data[2])) {
            name.put("middle", data[2]);
        }
        name.put("last", data[3]);

        JSONObject jso = new JSONObject();
        jso.put("id", Long.valueOf(data[0]));
        jso.put("name", name);
        jso.put("phone", data[4]);

        return jso.toString();
    }
}
