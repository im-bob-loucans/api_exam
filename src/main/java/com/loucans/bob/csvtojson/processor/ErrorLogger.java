package com.loucans.bob.csvtojson.processor;

public interface ErrorLogger {
    void logError(Integer rowNum, String errorToLog);
}
