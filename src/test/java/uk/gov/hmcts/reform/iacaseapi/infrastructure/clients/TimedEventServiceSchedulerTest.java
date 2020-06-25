package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import feign.FeignException;
import java.time.ZonedDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.AccessTokenProvider;

@RunWith(MockitoJUnitRunner.class)
public class TimedEventServiceSchedulerTest {

    @Mock private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock private AccessTokenProvider accessTokenProvider;
    @Mock private TimedEventServiceApi timedEventServiceApi;

    TimedEventServiceScheduler timedEventServiceScheduler;

    private String s2sToken = "someS2sToken";
    private String authToken = "authToken";

    @Before
    public void setup() {
        when(serviceAuthTokenGenerator.generate()).thenReturn(s2sToken);
        when(accessTokenProvider.getAccessToken()).thenReturn(authToken);

        timedEventServiceScheduler = new TimedEventServiceScheduler(serviceAuthTokenGenerator, accessTokenProvider, timedEventServiceApi);
    }


    @Test
    public void should_invoke_timed_event_api() {

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
    public void should_rethrow_exception_from_api() {

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