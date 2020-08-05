package com.loucans.bob.csvtojson;

import com.loucans.bob.csvtojson.processor.CsvProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

public class CsvToJsonMonitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvToJsonMonitor.class);

    private final ExecutorService executorService =
            new ThreadPoolExecutor(5, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    // - ---------------------------------------
    // - Starts the directory monitoring process
    // - ---------------------------------------
    public void start(String inputPath,
                       String outputPath,
                       String errorPath) {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            LOGGER.debug("registering inputPath file monitor, inputPath: [{}]", inputPath);
            Path dir = Paths.get(inputPath);
            dir.register(watchService, ENTRY_CREATE);

            for (; ;) {
                LOGGER.debug("polling inputPath for events");
                WatchKey key = watchService.take();
                List<WatchEvent<?>> watchEvents = key.pollEvents();

                // FLUP - need to filter events - i.e. directory create events...
                // FLUP - for large files,  need to wait until the copy or write is done
                watchEvents.forEach((event) -> {
                    LOGGER.debug("processing event, context: [{}]", event.context());
                    Path created = (Path) event.context();
                    if (StringUtils.endsWith(created.toString(), ".csv")) {

                        // FLUP - consider a way to signal to thread to stop processing
                        executorService.execute(() -> {
                            LOGGER.debug(
                                    "processing new csv file on thread, filename: [{}], thread: [{}]",
                                    created.toString(), Thread.currentThread().getName());

                            // FLUP - need a way to capture thread errors and log
                            // FLUP - consider initializing processor using a builder
                            new CsvProcessor()
                                    .processCsvFile(
                                            created.toString(),
                                            inputPath,
                                            outputPath,
                                            errorPath);

                            LOGGER.debug(
                                    "processing complete, filename: [{}], thread: [{}]",
                                    created.toString(), Thread.currentThread().getName());

                        });
                    }
                });
                key.reset();
            }
        } catch (Throwable t) {
            LOGGER.error("exiting an unexpected error occurred monitoring input path", t);
            System.exit(1);
        } finally {
            executorService.shutdown();
        }
    }
}

