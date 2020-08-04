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

import static java.lang.String.format;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class Parsing {

    private CsvProcessor service;
    private OutputWriter mockOutput;
    private ErrorLogger mockError;
    private List<String> output;
    private List<String> errors;

    @Before
    public void setup() {
        service = new CsvProcessor();
        output = new ArrayList<>();
        mockOutput = toWrite -> output.add(toWrite);

        errors = new ArrayList<>();
        mockError = errorToLog -> errors.add(errorToLog);
    }

    @Test
    public void process_happyPath() {
        service.processCsvFile(csvFile("happy_path"), mockOutput, mockError);
        assertEquals(
                "[\n" +
                 "{\"phone\":\"555-555-5550\",\"name\":{\"middle\":\"middle_name_0\",\"last\":\"last_name_0\",\"first\":\"first_name_0\"},\"id\":12345670},\n" +
                 "{\"phone\":\"555-555-5551\",\"name\":{\"middle\":\"middle_name_1\",\"last\":\"last_name_1\",\"first\":\"first_name_1\"},\"id\":12345671}\n" +
                "]",
                StringUtils.join(output, ""));
        assertEquals(2, new JSONArray(StringUtils.join(output, "")).length());
    }

    @Test
    public void process_emptyFile() {
        service.processCsvFile(csvFile("empty_file"), mockOutput, mockError);
        assertEquals(
                "[]",
                StringUtils.join(output, ""));
        assertEquals(0, new JSONArray(StringUtils.join(output, "")).length());
    }

    @Test
    public void process_emptyValues() {
        service.processCsvFile(csvFile("empty_values"), mockOutput, mockError);
        assertEquals(
                "[\n" +
                 "{\"phone\":\"555-555-5552\",\"name\":{\"last\":\"last_name_2\",\"first\":\"first_name_2\"},\"id\":12345672}\n" +
                "]",
                StringUtils.join(output, ""));
        assertEquals(1, new JSONArray(StringUtils.join(output, "")).length());
    }

    @Test
    public void process_InvalidValues() {
        service.processCsvFile(csvFile("invalid_values"), mockOutput, mockError);
        assertEquals("[]",
                StringUtils.join(output, ""));
        assertEquals(0, new JSONArray(StringUtils.join(output, "")).length());
    }

    @Test
    public void process_spacesValues() {
        service.processCsvFile(csvFile("spaces_for_values"), mockOutput, mockError);
        assertEquals(
                "[\n" +
                 "{\"phone\":\"555-555-5556\",\"name\":{\"middle\":\"middle_name_6\",\"last\":\"last_name_6\",\"first\":\" \"},\"id\":12345676},\n" +
                 "{\"phone\":\"555-555-5556\",\"name\":{\"middle\":\" \",\"last\":\"last_name_6\",\"first\":\"first_name_6\"},\"id\":12345676},\n" +
                 "{\"phone\":\"555-555-5556\",\"name\":{\"middle\":\"middle_name_6\",\"last\":\" \",\"first\":\"first_name_6\"},\"id\":12345676}\n" +
                "]",
                StringUtils.join(output, ""));
        assertEquals(3, new JSONArray(StringUtils.join(output, "")).length());
    }

    @Test
    public void process_invalidHeaders() {
        try {
            service.processCsvFile(csvFile("invalid_headers"), mockOutput, mockError);
        } catch (InvalidDataRowException e) {

        }
        assertEquals("", StringUtils.join(output, ""));

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
}
