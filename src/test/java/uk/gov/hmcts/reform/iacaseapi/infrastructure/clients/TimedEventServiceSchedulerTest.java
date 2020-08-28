package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import feign.FeignException;
import java.time.ZonedDateTime;
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

    @Mock private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock private AccessTokenProvider accessTokenProvider;
    @Mock private TimedEventServiceApi timedEventServiceApi;

    TimedEventServiceScheduler timedEventServiceScheduler;

    String s2sToken = "someS2sToken";
    String authToken = "authToken";

    @BeforeEach
    void setUp() {

        when(serviceAuthTokenGenerator.generate()).thenReturn(s2sToken);
        when(accessTokenProvider.getAccessToken()).thenReturn(authToken);

        timedEventServiceScheduler = new TimedEventServiceScheduler(serviceAuthTokenGenerator, accessTokenProvider, timedEventServiceApi);
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
            .isInstanceOf(AsylumCaseServiceResponseException.class)
            .hasCauseInstanceOf(FeignException.class);
    }
}