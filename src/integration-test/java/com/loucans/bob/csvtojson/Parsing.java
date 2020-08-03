package com.loucans.bob.csvtojson;

import com.loucans.bob.csvtojson.processor.CsvProcessor;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Objects;

import static java.lang.String.format;

public class Parsing {

    private CsvProcessor service;

    @Before
    public void setup() {
        service = new CsvProcessor();
    }

//    @Test
//    public void process_happyPath() {
//            service.processCsvFile(csvFile("happy_path"), outputFile, errorFile);
//    }
//
//    @Test
//    public void process_emptyValues() {
//        service.processCsvFile(csvFile("empty_values"), outputFile, errorFile);
//    }
//
//    @Test
//    public void process_InvalidValues() {
//        service.processCsvFile(csvFile("invalid_values"), outputFile, errorFile);
//    }
//
//    @Test
//    public void process_spacesValues() {
//        service.processCsvFile(csvFile("spaces_for_values"), outputFile, errorFile);
//    }
//
//    @Test
//    public void process_invalidHeaders() {
//        service.processCsvFile(csvFile("invalid_headers"), outputFile, errorFile);
//    }

    private File csvFile(String testFileName) {
        ClassLoader classLoader = getClass().getClassLoader();

        URL resource = classLoader.getResource(
                format("com/loucans/bob/csvtojson/csvs/%s.csv", testFileName));
        if (Objects.isNull(resource)) {
            throw new RuntimeException();
        }

        return new File(resource.getFile());
    }
}
