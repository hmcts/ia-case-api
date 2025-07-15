package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.advice;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class ErrorResponseLoggerTest {

    @Mock
    private PreSubmitCallbackResponse preSubmitCallbackResponse;

    @Mock
    private RestClientResponseException restClientResponseException;

    @InjectMocks
    private ErrorResponseLogger errorResponseLogger;

    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    public void setup() {

        Logger responseLogger = (Logger) LoggerFactory.getLogger(ErrorResponseLogger.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        responseLogger.addAppender(listAppender);

    }

    @Test
    public void should_handle_rest_client_exception_response_and_log() {

        String jsonResponseBody = "{\"succeeded\":false}";

        when(restClientResponseException.getStatusCode()).thenReturn(HttpStatus.BAD_GATEWAY);
        when(restClientResponseException.getResponseBodyAsString()).thenReturn(jsonResponseBody);

        errorResponseLogger.maybeLogException(restClientResponseException);

        List<ILoggingEvent> logEvents = this.listAppender.list;
        assertEquals(logEvents.size(), 1);
        assertThat(logEvents.get(0).getFormattedMessage())
                .startsWith("Error returned with status: "
                        + HttpStatus.BAD_GATEWAY.value()
                        + ". \nWith response body: "
                        + jsonResponseBody);

        verify(restClientResponseException).getStatusCode().value();
        verify(restClientResponseException).getResponseBodyAsString();
    }

    @Test
    public void should_handle_rest_client_exception_response_and_not_print_case_data() {

        String jsonResponseBody = "{\"data\": {\"appellantGivenNames\":\"Test\",\"appellantFamilyName\":\"User\"}}";

        when(restClientResponseException.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(restClientResponseException.getResponseBodyAsString()).thenReturn(jsonResponseBody);

        errorResponseLogger.maybeLogException(restClientResponseException);

        List<ILoggingEvent> logEvents = this.listAppender.list;
        assertEquals(logEvents.size(), 1);

        assertThat(logEvents.get(0).getFormattedMessage())
                .startsWith("Error returned with status: "
                        + HttpStatus.INTERNAL_SERVER_ERROR.value()
                        + ". \nWith response body: ");

        verify(restClientResponseException).getStatusCode().value();
        verify(restClientResponseException).getResponseBodyAsString();
    }

    @Test
    public void should_ignore_generic_exception_response() {

        Exception exception = mock(Exception.class);

        errorResponseLogger.maybeLogException(exception);

        List<ILoggingEvent> logEvents = this.listAppender.list;
        assertEquals(logEvents.size(), 0);

        verifyNoInteractions(restClientResponseException);

    }

}
