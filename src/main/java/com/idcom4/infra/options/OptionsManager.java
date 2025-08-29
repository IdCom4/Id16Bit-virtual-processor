package com.idcom4.infra.options;

import java.util.Arrays;

public class OptionsManager {

    public static Options GetOptions(String[] args) {

        Options options = new Options();
        boolean defaultOptionSet = false;

        for (String arg : args) {
            String[] parts = arg.split("=");

            Options.SupportedOption option = GetSupportedOption(parts[0]);
            if (option == null) {
                if (parts.length == 1 && !defaultOptionSet && Options.defaultAnonymousOption != null) {
                    Options.defaultAnonymousOption.parser().parse(options, parts[0]);
                    defaultOptionSet = true;
                } else {
                    Options.UnknownOption(parts[0]);
                }

                continue;
            };

            option.parser().parse(options, parts.length > 1 ? parts[1] : null);
        }

        return options;
    }

    private static Options.SupportedOption GetSupportedOption(String arg) {
        return Arrays.stream(Options.supportedOptions)
                .filter(opt -> opt.name().equals(arg) || opt.shortName().equals(arg))
                .findFirst().orElse(null);
    }

}
