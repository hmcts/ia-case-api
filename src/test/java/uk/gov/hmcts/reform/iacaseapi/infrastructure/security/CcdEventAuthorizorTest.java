package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.util.LoggerUtil;

@ExtendWith(MockitoExtension.class)
class CcdEventAuthorizorTest {

    @Mock private UserDetailsProvider userDetailsProvider;
    @Mock private UserDetails userDetails;

    CcdEventAuthorizor ccdEventAuthorizor;
    ListAppender<ILoggingEvent> loggingEventListAppender;

    @BeforeEach
    void setUp() {

        ccdEventAuthorizor =
            new CcdEventAuthorizor(
                ImmutableMap
                    .<String, List<Event>>builder()
                    .put("caseworker-role", Arrays.asList(Event.REQUEST_RESPONDENT_REVIEW, Event.SEND_DIRECTION))
                    .put("legal-role", Arrays.asList(Event.SUBMIT_APPEAL, Event.BUILD_CASE))
                    .build(),
                userDetailsProvider
            );

        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);

        loggingEventListAppender = LoggerUtil.getListAppenderForClass(CcdEventAuthorizor.class);
    }

    @Test
    void does_not_throw_access_denied_exception_if_role_is_allowed_access_to_event() {

        when(userDetails.getRoles()).thenReturn(
            Arrays.asList("some-unrelated-role", "legal-role")
        );

        assertThatCode(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.BUILD_CASE))
            .doesNotThrowAnyException();

        when(userDetails.getRoles()).thenReturn(
            Arrays.asList("caseworker-role", "some-unrelated-role")
        );

        assertThatCode(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.SEND_DIRECTION))
            .doesNotThrowAnyException();

        assertThat(loggingEventListAppender.list)
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(Tuple.tuple("Request within CcdEventAuthorizor for event: {} processed in {}ms", Level.INFO));
    }

    @Test
    void throw_access_denied_exception_if_role_not_allowed_access_to_event() {

        when(userDetails.getRoles()).thenReturn(
            Arrays.asList("caseworker-role", "some-unrelated-role")
        );

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.BUILD_CASE))
            .hasMessage("Event 'buildCase' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);

        assertThat(loggingEventListAppender.list)
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(Tuple.tuple("Access Denied Exception thrown within CcdEventAuthorizor for event: {}", Level.ERROR));

        when(userDetails.getRoles()).thenReturn(
            Arrays.asList("some-unrelated-role", "legal-role")
        );

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.SEND_DIRECTION))
            .hasMessage("Event 'sendDirection' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);

        assertThat(loggingEventListAppender.list)
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(Tuple.tuple("Access Denied Exception thrown within CcdEventAuthorizor for event: {}", Level.ERROR));
    }

    @Test
    void throw_access_denied_exception_if_event_not_configured() {

        when(userDetails.getRoles()).thenReturn(
            Arrays.asList("caseworker-role", "some-unrelated-role")
        );

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.UPLOAD_RESPONDENT_EVIDENCE))
            .hasMessage("Event 'uploadRespondentEvidence' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);

        assertThat(loggingEventListAppender.list)
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(Tuple.tuple("Access Denied Exception thrown within CcdEventAuthorizor for event: {}", Level.ERROR));
    }

    @Test
    void throw_access_denied_exception_if_user_has_no_roles() {

        when(userDetails.getRoles()).thenReturn(
            Collections.emptyList()
        );

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.BUILD_CASE))
            .hasMessage("Event 'buildCase' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);

        assertThat(loggingEventListAppender.list)
            .extracting(ILoggingEvent::getMessage, ILoggingEvent::getLevel)
            .contains(Tuple.tuple("Access Denied Exception thrown within CcdEventAuthorizor for event: {}", Level.ERROR));
    }
}
