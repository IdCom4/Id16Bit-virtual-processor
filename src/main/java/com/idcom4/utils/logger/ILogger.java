package com.idcom4.utils.logger;

public interface ILogger {
    void log(String message, Object ...args);
    void logln(String message, Object ...args);
    void warn(String message, Object ...args);
    void warnln(String message, Object ...args);
    void err(String message, Object ...args);
    void errln(String message, Object ...args);
}
