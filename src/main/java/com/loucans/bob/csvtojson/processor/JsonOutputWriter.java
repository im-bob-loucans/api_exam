package com.loucans.bob.csvtojson.processor;

import com.loucans.bob.csvtojson.model.CsvRow;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static java.lang.System.lineSeparator;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class JsonOutputWriter implements OutputWriter {
    private final Path outputFile;
    private boolean fileCreated = false;
    private boolean firstRecordWritten = false;

    public JsonOutputWriter(String outputFilePath) {
        outputFile = Paths.get(outputFilePath);
        try {
            Files.deleteIfExists(outputFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeRecord(CsvRow csvRow) {
        try {
            if (!fileCreated) {
                Files.createFile(outputFile);
                Files.writeString(outputFile, "[", UTF_8, APPEND);
                fileCreated = true;
            }

            if (!firstRecordWritten) {
                if (Objects.nonNull(csvRow)) {
                    Files.writeString(outputFile, lineSeparator() + rowtoJson(csvRow), UTF_8, APPEND);
                    firstRecordWritten = true;
                }
            } else {
                if (Objects.nonNull(csvRow)) {
                    Files.writeString(outputFile, "," + lineSeparator() + rowtoJson(csvRow), UTF_8, APPEND);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        if (fileCreated) {
            Files.writeString(outputFile, (firstRecordWritten ? lineSeparator() : "") + "]", UTF_8, APPEND);
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
