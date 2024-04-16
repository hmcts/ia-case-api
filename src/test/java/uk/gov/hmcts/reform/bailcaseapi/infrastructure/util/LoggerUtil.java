package uk.gov.hmcts.reform.bailcaseapi.infrastructure.util;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.slf4j.LoggerFactory;

public final class LoggerUtil {

    private LoggerUtil() {
    }

    public static ListAppender<ILoggingEvent> getListAppenderForClass(Class loggerClass) {

        Logger logger = (Logger) LoggerFactory.getLogger(loggerClass);

        ListAppender<ILoggingEvent> loggingEventListAppender = new ListAppender<>();

        loggingEventListAppender.start();

        logger.addAppender(loggingEventListAppender);

        return loggingEventListAppender;
    }
}
