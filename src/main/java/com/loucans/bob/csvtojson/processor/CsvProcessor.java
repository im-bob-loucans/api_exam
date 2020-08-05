package com.loucans.bob.csvtojson.processor;

import com.loucans.bob.csvtojson.exception.InvalidDataRowException;
import com.loucans.bob.csvtojson.exception.InvalidDataValueException;
import com.loucans.bob.csvtojson.model.CsvRow;
import com.loucans.bob.csvtojson.parser.CommonsCsvParser;
import com.loucans.bob.csvtojson.parser.CsvParser;
import com.loucans.bob.csvtojson.parser.CsvRowCallbackErrorHandler;
import com.loucans.bob.csvtojson.parser.CsvRowCallbackHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.*;

// - --------------------------------------------------
// - main logic for processing a file per requirements
// - FLUP - this class is need to be broken up and redesigned
//        - break up this callback approach and just process
//          file row by row would have been better
// - --------------------------------------------------
public class CsvProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvProcessor.class);

    public void processCsvFile(String csvFileName,
                               String inputPath,
                               String outputPath,
                               String errorPath) {
        try (CsvParser parser =
                     new CommonsCsvParser(inputPath + "/" + csvFileName);
             OutputWriter outputWriter =
                     new JsonOutputWriter(outputPath + "/" + substring(csvFileName, 0, lastIndexOf(csvFileName, ".")) + ".json");
             ErrorLogger errorLogger =
                     new CsvErrorLogger(errorPath + "/" + csvFileName)) {

            AtomicInteger rowsProcessed = new AtomicInteger(0);

            CsvRowCallbackHandler rowHandler = (rowNum, csvRow) -> {
                if (isHeaderRow(rowNum)) {
                    assertValidHeaderRow(csvRow);
                    rowsProcessed.incrementAndGet();
                } else {
                    assertValidDataRow(csvRow);
                    outputWriter.writeRecord(csvRow);
                    LOGGER.debug("processed row: [{}]", csvRow);
                    rowsProcessed.incrementAndGet();
                }
            };

            CsvRowCallbackErrorHandler errorHandler = (rowNum, error) -> {
                LOGGER.error(
                        "row failed, value: [{}], error: [{}]", error.getValue(), error.getMessage());
                errorLogger.logError(
                        rowNum, error.getMessage().replace("\"", "\"\""));
                if (isHeaderRow(rowNum)) {
                    // abort for processing errors on header row
                    throw error;
                }
            };

            parser.parse(rowHandler, errorHandler);

            if (rowsProcessed.intValue() == 0) {
                errorLogger.logError(0, "\"empty file\"");
            } else if (rowsProcessed.intValue() == 1) {
                outputWriter.writeRecord(null);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                Files.delete(Paths.get(inputPath + "/" + csvFileName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private boolean isHeaderRow(Integer rowNum) {
        return rowNum == 1;
    }

    private void assertValidHeaderRow(CsvRow csvRow) {
        if (isNull(csvRow) || isNull(csvRow.getRowData()) || csvRow.getRowData().length != 5) {
            throw new InvalidDataRowException(
                    "csv header row is null or does not match expected header definition", csvRow);
        }

        String[] headers = {"INTERNAL_ID", "FIRST_NAME", "MIDDLE_NAME", "LAST_NAME", "PHONE_NUM"};
        for (int i = 0; i < headers.length; i++) {
            if (!headers[i].equals(csvRow.getRowData()[i])) {
                throw new InvalidDataRowException(
                        "csv header row is null or does not match expected header definition", csvRow);
            }
        }
    }

    private void assertValidDataRow(CsvRow csvRow) {
        if (isNull(csvRow) || isNull(csvRow.getRowData()) || csvRow.getRowData().length != 5) {
            throw new InvalidDataRowException(
                    "csv data row is null or does not match expected header definition", csvRow);
        }

        String[] data = csvRow.getRowData();
        if (isEmpty(data[0]) || !data[0].matches("\\d{8}")) {
            throw new InvalidDataValueException(
                    "invalid INTERNAL_ID [required, length <= 8, digits only]", data[0]);
        }

        if (isEmpty(data[1]) || data[1].length() > 15) {
            throw new InvalidDataValueException(
                    "invalid FIRST_NAME [required, length <= 15]", data[1]);
        }

        if (!isEmpty(data[2]) && data[2].length() > 15) {
            throw new InvalidDataValueException(
                    "invalid MIDDLE_NAME [length <= 15]", data[2]);
        }

        if (isEmpty(data[3]) || data[3].length() > 15) {
            throw new InvalidDataValueException(
                    "invalid LAST_NAME [required, length <= 15]", data[3]);
        }

        if (isEmpty(data[4]) || !(data[4].length() == 12) || !data[4].matches("\\d{3}-\\d{3}-\\d{4}")) {
            throw new InvalidDataValueException(
                    "invalid PHONE_NUM [required, length = 12, format: ###-###-####]", data[4]);
        }
    }
}
