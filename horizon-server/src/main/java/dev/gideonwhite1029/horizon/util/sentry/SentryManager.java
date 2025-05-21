package dev.gideonwhite1029.horizon.util.sentry;

import io.sentry.Sentry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SentryManager {

    private static final Logger LOGGER = LogManager.getLogger(SentryManager.class);

    private SentryManager() {

    }

    private static boolean initialized = false;

    public static synchronized void init(Level logLevel) {
        if (initialized) {
            return;
        }
        if (logLevel == null) {
            LOGGER.error("Invalid log level, defaulting to WARN.");
            logLevel = Level.WARN;
        }
        try {
            initialized = true;

            Sentry.init(options -> {
                options.setDsn(dev.gideonwhite1029.horizon.HorizonConfig.sentryDsn);
                options.setMaxBreadcrumbs(100);
            });

            HorizonSentryAppender appender = new HorizonSentryAppender(logLevel);
            appender.start();
            ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addAppender(appender);
            LOGGER.info("Sentry logging started!");
        } catch (Exception e) {
            LOGGER.warn("Failed to initialize sentry!", e);
            initialized = false;
        }
    }

}
