package com.loucans.bob.csvtojson.processor;

import java.io.Closeable;

public interface ErrorLogger extends Closeable {
    void logError(Integer rowNum, String errorToLog);
}
