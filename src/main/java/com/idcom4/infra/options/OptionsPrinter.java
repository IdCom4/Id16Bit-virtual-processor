package com.idcom4.infra.options;

import java.util.Arrays;
import java.util.Comparator;

public class OptionsPrinter {
    public static void PrintAvailableOptions() {
        Options.SupportedOption[] supportedOptions = Options.supportedOptions;

        System.out.println("\nAvailable options:\n");
        // get max name
        int maxNameLength = Arrays.stream(supportedOptions).max(Comparator.comparingInt(opt -> opt.name().length())).get().name().length();
        if (Options.defaultAnonymousOption != null && maxNameLength < Options.defaultAnonymousOption.name().length())
            maxNameLength = Options.defaultAnonymousOption.name().length();

        // get max short name
        int maxShortNameLength = Arrays.stream(supportedOptions).max(Comparator.comparingInt(opt -> opt.shortName().length())).get().shortName().length();
        if (Options.defaultAnonymousOption != null && maxShortNameLength < Options.defaultAnonymousOption.shortName().length())
            maxShortNameLength = Options.defaultAnonymousOption.shortName().length();

        // get max description
        int maxDescLength = Arrays.stream(supportedOptions).max(Comparator.comparingInt(opt -> opt.description().length())).get().description().length();
        if (Options.defaultAnonymousOption != null && maxDescLength < Options.defaultAnonymousOption.description().length())
            maxDescLength = Options.defaultAnonymousOption.description().length();

        String format = "%-" + (maxNameLength + 2) + "s%-" +  (maxShortNameLength + 10) + "s%-" + (maxDescLength) + "s\n";

        if (Options.defaultAnonymousOption != null)
            System.out.format(format, Options.defaultAnonymousOption.name(), Options.defaultAnonymousOption.shortName(), Options.defaultAnonymousOption.description());

        for (Options.SupportedOption supportedOption : supportedOptions) {
            System.out.format(format, supportedOption.name(), supportedOption.shortName(), supportedOption.description());
        }
    }
}
