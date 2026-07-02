package uk.gov.hmcts.reform.iacaseapi.utils;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.params.provider.Arguments;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class TestUtils {
    private static Stream<Arguments> eventAndCallbackStages() {
        return Stream.of(
            Event.values()
        ).flatMap(event ->
            Stream.of(PreSubmitCallbackStage.values())
                .map(stage -> Arguments.of(event, stage))
        );
    }

    public static ListAppender<ILoggingEvent> setupLogVerifier(Class someClass) {
        Logger responseLogger = (Logger) LoggerFactory.getLogger(someClass);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        responseLogger.addAppender(listAppender);
        return listAppender;
    }

    public static ILoggingEvent verifyLogsContainMessage(ListAppender<ILoggingEvent> listAppender, String expectedMessage) {
        List<ILoggingEvent> logEvents = listAppender.list;
        List<ILoggingEvent> loggingEvent =
            logEvents.stream().filter(log -> log.getFormattedMessage().contains(expectedMessage)).toList();
        assertFalse(loggingEvent.isEmpty());
        return loggingEvent.getFirst();
    }
}
