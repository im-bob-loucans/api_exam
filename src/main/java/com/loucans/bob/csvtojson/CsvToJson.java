package com.loucans.bob.csvtojson;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CsvToJson {
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvToJson.class);

    // - ---------------------------------------
    // - main entry point
    // - ---------------------------------------
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
}

