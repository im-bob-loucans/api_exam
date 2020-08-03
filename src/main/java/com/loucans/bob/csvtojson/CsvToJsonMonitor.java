package com.loucans.bob.csvtojson;

import com.loucans.bob.csvtojson.processor.CsvProcessor;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
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
    // - main entry point
    // - ---------------------------------------
    //
    public static void main(String[] args) {
        LOGGER.debug("launching: args: [{}]", (Object) args);

        Options options = new Options();
        options.addOption(newOption("e", "errorPath", "output directory for error files"));
        options.addOption(newOption("i", "inputPath", "input directory to monitor for csv files"));
        options.addOption(newOption("o", "outputPath", "output directory for json files"));

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            new HelpFormatter().printHelp("csvToJsonMonitor", options);
            System.exit(1);
        }

        new CsvToJsonMonitor().start(
                cmd.getOptionValue("inputPath"),
                cmd.getOptionValue("outputPath"),
                cmd.getOptionValue("errorPath"));
    }

    private static Option newOption(String opt, String longOpt, String description) {
        Option option = new Option(opt, longOpt, true, description);
        option.setRequired(true);
        return option;
    }

    // - ---------------------------------------
    // - Start the directory monitoring process
    // - ---------------------------------------
    //
    // FLUP - extract to own class
    private void start(String inputPath,
                       String outputPath,
                       String errorPath) {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            LOGGER.debug("registering inputPath file monitor, inputPath: [{}]", inputPath);
            Path dir = Paths.get(inputPath);
            dir.register(watchService, ENTRY_CREATE);

            for (; ; ) {
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
                        });
                    }
                });
                key.reset();
            }
        } catch (Throwable t) {
            LOGGER.error("exiting an unexpected error occured monitoring input path", t);
            System.exit(1);
        } finally {
            executorService.shutdown();
        }
    }
}

