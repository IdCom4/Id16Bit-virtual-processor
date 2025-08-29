package com.idcom4.utils.logger;

public class ConsoleLogger implements ILogger {

    private boolean enabled = true;

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    @Override
    public void log(String message, Object ...args) {
        if (enabled) {
            String value = args.length > 0 ? String.format(message, args) : message;
            System.out.print("[LOG] " + value);
        }
    }

    @Override
    public void logln(String message, Object... args) {
        this.log(message + "\n", args);
    }

    @Override
    public void warn(String message, Object ...args) {
        if (enabled) {
            String value = args.length > 0 ? String.format(message, args) : message;
            System.out.print("[WARN] " + value);
        }
    }

    @Override
    public void warnln(String message, Object... args) {
        this.warn(message + "\n", args);
    }

    @Override
    public void err(String message, Object ...args) {
        if (enabled) {
            String value = args.length > 0 ? String.format(message, args) : message;
            System.err.print("[ERR] " + value);
        }
    }

    @Override
    public void errln(String message, Object... args) {
        this.err(message + "\n", args);
    }
}
