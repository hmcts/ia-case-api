package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import feign.FeignException;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.AccessTokenProvider;

@ExtendWith(MockitoExtension.class)
class TimedEventServiceSchedulerTest {

    TimedEventServiceScheduler timedEventServiceScheduler;
    @Mock
    private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock
    private AccessTokenProvider accessTokenProvider;
    @Mock
    private TimedEventServiceApi timedEventServiceApi;
    private String s2sToken = "someS2sToken";
    private String authToken = "authToken";

    @BeforeEach
    public void setup() {
        when(serviceAuthTokenGenerator.generate()).thenReturn(s2sToken);
        when(accessTokenProvider.getAccessToken()).thenReturn(authToken);

        timedEventServiceScheduler =
            new TimedEventServiceScheduler(serviceAuthTokenGenerator, accessTokenProvider, timedEventServiceApi);
    }


    @Test
    void should_invoke_timed_event_api() {

        TimedEvent timedEvent = new TimedEvent(
            "someId",
            Event.REQUEST_HEARING_REQUIREMENTS_FEATURE,
            ZonedDateTime.now(),
            "",
            "",
            1234
        );

        when(timedEventServiceApi.submitTimedEvent(authToken, s2sToken, timedEvent)).thenReturn(timedEvent);

        assertEquals(timedEvent, timedEventServiceScheduler.schedule(timedEvent));
    }

    @Test
    void should_rethrow_exception_from_api() {

        TimedEvent timedEvent = new TimedEvent(
            "someId",
            Event.REQUEST_HEARING_REQUIREMENTS_FEATURE,
            ZonedDateTime.now(),
            "",
            "",
            1234
        );

        when(timedEventServiceApi.submitTimedEvent(authToken, s2sToken, timedEvent)).thenThrow(FeignException.class);

        assertThatThrownBy(() -> timedEventServiceScheduler.schedule(timedEvent))
            .isInstanceOf(ServiceResponseException.class)
            .hasCauseInstanceOf(FeignException.class);
    }

    @Test
    void should_invoke_delete_api_successfully() {
        // Given
        doNothing().when(timedEventServiceApi).deleteTimedEvent(authToken, s2sToken, "1234567");

        // When
        boolean result = timedEventServiceScheduler.deleteSchedule("1234567");

        // Then
        verify(timedEventServiceApi).deleteTimedEvent(authToken, s2sToken, "1234567");
        assertThat(result).isTrue();
    }

    @Test
    void should_invoke_delete_api_with_failure() {
        // Given
        doThrow(FeignException.class).when(timedEventServiceApi).deleteTimedEvent(authToken, s2sToken, "1234567");

        // When
        boolean result = timedEventServiceScheduler.deleteSchedule("1234567");

        // Then
        verify(timedEventServiceApi).deleteTimedEvent(authToken, s2sToken, "1234567");
        assertThat(result).isFalse();
    }

    @Test
    void should_schedule_timed_event_with_given_date() {
        // Given
        String caseId = "1234567890";
        ZonedDateTime scheduledDate = ZonedDateTime.now().plusDays(1);
        Event event = Event.REQUEST_HEARING_REQUIREMENTS_FEATURE;

        TimedEvent expectedTimedEvent = new TimedEvent(
            "",
            event,
            scheduledDate,
            "IA",
            "Asylum",
            Long.parseLong(caseId)
        );

        when(timedEventServiceApi.submitTimedEvent(eq(authToken), eq(s2sToken), any(TimedEvent.class)))
            .thenReturn(expectedTimedEvent);

        // When
        timedEventServiceScheduler.scheduleTimedEvent(caseId, scheduledDate, event, "");

        // Then
        verify(timedEventServiceApi).submitTimedEvent(eq(authToken), eq(s2sToken), any(TimedEvent.class));
    }

    @Test
    void should_schedule_timed_event_now() {
        // Given
        String caseId = "1234567890";
        Event event = Event.REQUEST_HEARING_REQUIREMENTS_FEATURE;
        LocalDateTime now = LocalDateTime.now();

        when(timedEventServiceApi.submitTimedEvent(eq(authToken), eq(s2sToken), any(TimedEvent.class)))
            .thenReturn(new TimedEvent("", event, ZonedDateTime.now(), "IA", "Asylum", Long.parseLong(caseId)));

        // When
        timedEventServiceScheduler.scheduleTimedEventNow(caseId, event);

        // Then
        verify(timedEventServiceApi).submitTimedEvent(eq(authToken), eq(s2sToken), any(TimedEvent.class));
    }

}
