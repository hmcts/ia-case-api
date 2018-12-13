package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;

@RunWith(MockitoJUnitRunner.class)
public class CcdEventAuthorizorTest {

    @Mock private UserCredentialsProvider requestUserCredentialsProvider;

    private CcdEventAuthorizor ccdEventAuthorizor;

    @Before
    public void setUp() {

        ccdEventAuthorizor =
            new CcdEventAuthorizor(
                ImmutableMap
                    .<String, List<Event>>builder()
                    .put("caseworker-role", Arrays.asList(Event.REQUEST_RESPONDENT_REVIEW, Event.SEND_DIRECTION))
                    .put("legal-role", Arrays.asList(Event.SUBMIT_APPEAL, Event.BUILD_CASE))
                    .build(),
                requestUserCredentialsProvider
            );
    }

    @Test
    public void does_not_throw_access_denied_exception_if_role_is_allowed_access_to_event() {

        when(requestUserCredentialsProvider.getRoles()).thenReturn(
            Arrays.asList("some-unrelated-role", "legal-role")
        );

        ccdEventAuthorizor.throwIfNotAuthorized(Event.BUILD_CASE);

        when(requestUserCredentialsProvider.getRoles()).thenReturn(
            Arrays.asList("caseworker-role", "some-unrelated-role")
        );

        ccdEventAuthorizor.throwIfNotAuthorized(Event.SEND_DIRECTION);
    }

    @Test
    public void throw_access_denied_exception_if_role_not_allowed_access_to_event() {

        when(requestUserCredentialsProvider.getRoles()).thenReturn(
            Arrays.asList("caseworker-role", "some-unrelated-role")
        );

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.BUILD_CASE))
            .hasMessage("Event 'buildCase' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);

        when(requestUserCredentialsProvider.getRoles()).thenReturn(
            Arrays.asList("some-unrelated-role", "legal-role")
        );

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.SEND_DIRECTION))
            .hasMessage("Event 'sendDirection' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void throw_access_denied_exception_if_event_not_configured() {

        when(requestUserCredentialsProvider.getRoles()).thenReturn(
            Arrays.asList("caseworker-role", "some-unrelated-role")
        );

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.UPLOAD_RESPONDENT_EVIDENCE))
            .hasMessage("Event 'uploadRespondentEvidence' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);
    }

    @Test
    public void throw_access_denied_exception_if_user_has_no_roles() {

        when(requestUserCredentialsProvider.getRoles()).thenReturn(
            Collections.emptyList()
        );

        assertThatThrownBy(() -> ccdEventAuthorizor.throwIfNotAuthorized(Event.BUILD_CASE))
            .hasMessage("Event 'buildCase' not allowed")
            .isExactlyInstanceOf(AccessDeniedException.class);
    }
}
