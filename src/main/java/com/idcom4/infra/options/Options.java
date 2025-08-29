package com.idcom4.infra.options;

public class Options {

    public enum EOptions {
        MMAP("--mmap", "-m"),
        DELAY("--delay", "-d"),
        LOG("--logs", "-l"),
        HELP("--help", "-h");

        public final String name;
        public final String shortName;

        EOptions(String name, String shortName) {
            this.name = name;
            this.shortName = shortName;
        }
    }

    public interface IArgValueParser {
        void parse(Options options, String value);
    }

    public record SupportedOption(String name, String shortName, String description, IArgValueParser parser) {}

    public static final SupportedOption[] supportedOptions = new SupportedOption[]{
        new SupportedOption(
                EOptions.MMAP.name, EOptions.MMAP.shortName,
                "The path to the memory map file",
                Options::SetMmapFile
        ),
        new SupportedOption(
                EOptions.DELAY.name, EOptions.DELAY.shortName,
                "The amount of delay between instructions, in milliseconds",
                Options::SetDelayInMS
        ),
        new SupportedOption(
                EOptions.LOG.name, EOptions.LOG.shortName,
                "Whether to log the execution state or not",
                Options::SetLog
        ),
        new SupportedOption(
                EOptions.HELP.name, EOptions.HELP.shortName,
                "Prints available options.",
                Options::SetHelp
        )
    };

    public static final SupportedOption defaultAnonymousOption = null;

    private String mmapFile = null;
    private int delayInMS = 0;
    private boolean log = false;
    private boolean help = false;

    public Options() {}

    private void SetMmapFile(String mmapFile) {
        this.mmapFile = mmapFile;
    }

    public String GetMmapFile() {
        return this.mmapFile;
    }

    private void SetDelayInMS(String value) {
        this.delayInMS = Math.max(0, Integer.parseInt(value));
    }

    private void SetLog(String _unused) {
        this.log = true;
    }

    private void SetHelp(String _unused) {
        this.help = true;
    }

    public int GetDelay() {
        return this.delayInMS;
    }
    public boolean GetLog() {
        return this.log;
    }
    public boolean GetHelp() {
        return this.help;
    }

    public static void UnknownOption(String unknownOption) {
        System.err.println("[ERR] Unknown option: " + unknownOption);
        System.err.println("use " + EOptions.HELP.name + " to see the available options.");
    }
}
