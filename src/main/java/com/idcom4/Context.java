package com.idcom4;

import com.idcom4.exceptions.IDException;
import com.idcom4.utils.logger.ILogger;

public class Context {

    public static Context INSTANCE;

    public final ILogger logger;

    private Context(ILogger logger) {
        this.logger = logger;
    }

    public static void initContext(ILogger logger) throws IDException {
        if (INSTANCE != null) throw new IDException("Context already initialized");

        INSTANCE = new Context(logger);
    }

}
