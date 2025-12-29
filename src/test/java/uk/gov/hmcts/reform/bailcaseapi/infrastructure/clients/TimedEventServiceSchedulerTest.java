package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.TimedEvent;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.AccessTokenProvider;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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
            Event.CASE_LISTING,
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
            Event.CASE_LISTING,
            ZonedDateTime.now(),
            "",
            "",
            1234
        );

        when(timedEventServiceApi.submitTimedEvent(authToken, s2sToken, timedEvent)).thenThrow(FeignException.class);

        assertThatThrownBy(() -> timedEventServiceScheduler.schedule(timedEvent))
            .isInstanceOf(AsylumCaseServiceResponseException.class)
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

}
