package com.loucans.bob.csvtojson.processor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;

public class CsvErrorLogger implements ErrorLogger {
    private final Path errorFile;
    private boolean fileCreated = false;

    public CsvErrorLogger(String errorFilePath) {
        this.errorFile =
                Paths.get(errorFilePath);
        try {
            Files.deleteIfExists(errorFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void logError(Integer rowNum, String errorMessage) {
        try {
            if (!fileCreated) {
                Files.createFile(errorFile);
                Files.writeString(
                        errorFile, "\"LINE_NUM\",\"ERROR_MSG\"\r\n", UTF_8, APPEND);
                fileCreated = true;
            }
            Files.writeString(errorFile, format("\"%d\",\"%s\"\r\n", rowNum, errorMessage), UTF_8, APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }
}
