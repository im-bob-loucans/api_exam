package com.loucans.bob.csvtojson.parser;

import com.loucans.bob.csvtojson.exception.CsvToJsonException;
import com.loucans.bob.csvtojson.exception.InvalidDataRowException;
import com.loucans.bob.csvtojson.model.CsvRow;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

// - --------------------------------------------------
// - Implementation of the CSV Parser using commons-csv
// - --------------------------------------------------
public class CommonsCsvParser implements CsvParser {

    private final FileReader fileReader;
    private final CSVParser parser;

    public CommonsCsvParser(File csvFile) {
        try {
            // FLUP - how to handle source file encoding
            // FLUP - using the standard CSV parser,  what should it be?
            fileReader = new FileReader(csvFile, UTF_8);
            parser = CSVFormat.RFC4180.parse(fileReader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CommonsCsvParser(String csvFilePath) {
        this(new File(csvFilePath));
    }

    public void parse(CsvRowCallbackHandler rowCallbackHandler,
                      CsvRowCallbackErrorHandler rowCallbackErrorHandler) {
        int rowNum = 1;
        try {
            for (CSVRecord record : parser) {
                if (nonNull(record) && (nonNull(rowCallbackHandler))) {
                    List<String> rowData = new ArrayList<>();
                    for (String s : record) {
                        rowData.add(s);
                    }

                    try {
                        rowCallbackHandler.handleRow(
                                rowNum,
                                new CsvRow(rowData.toArray(new String[]{})));
                    } catch (CsvToJsonException e) {
                        rowCallbackErrorHandler.handleError(rowNum, e);
                    }
                }
                rowNum++;
            }
        } catch (IllegalStateException e) {
            // commons-csv can throw row related errors while processing on hasNext
            // this catches those conditions and reports them as line errors.
            // processing will stop at this point and this will be the last reported error
            String message =
                    isBlank(e.getMessage())
                            ? "unknown error processing csv file"
                            : e.getMessage().replace("IOException reading next record: java.io.IOException: ", "");
            rowCallbackErrorHandler.handleError(rowNum, new InvalidDataRowException(message, null));
        }
    }

    @Override
    public void close() throws IOException {
        if (nonNull(parser)) {
            parser.close();
        }

        if (nonNull(fileReader)) {
            fileReader.close();
        }
    }
}
