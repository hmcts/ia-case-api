package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.bailcaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LaunchDarklyFeatureTogglerTest {

    @Mock
    private LDClientInterface ldClient;
    @Mock
    private UserDetailsProvider userDetailsProvider;
    @Mock
    private UserDetails userDetails;

    private LaunchDarklyFeatureToggler toggler;
    private final String userId = "user-id-123";
    private final String forename = "John";
    private final String surname = "Doe";
    private final String email = "john-doe@email.com";
    private final LDContext expectedContext = LDContext.builder(userId)
        .set("firstName", forename)
        .set("lastName", surname)
        .set("email", email)
        .build();

    @BeforeEach
    void setUp() {
        toggler = new LaunchDarklyFeatureToggler(ldClient, userDetailsProvider);
        when(userDetails.getId()).thenReturn(userId);
        when(userDetails.getForename()).thenReturn(forename);
        when(userDetails.getSurname()).thenReturn(surname);
        when(userDetails.getEmailAddress()).thenReturn(email);
    }

    @Test
    void should_return_true_when_flag_is_enabled() {
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(ldClient.boolVariation(anyString(), any(LDContext.class), anyBoolean())).thenReturn(true);

        boolean result = toggler.getValue("some-flag", false);

        assertTrue(result);
        verify(ldClient).boolVariation("some-flag", expectedContext, false);
    }

    @Test
    void should_return_false_when_flag_is_disabled() {
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(ldClient.boolVariation(anyString(), any(LDContext.class), anyBoolean())).thenReturn(false);

        boolean result = toggler.getValue("another-flag", true);

        assertFalse(result);
        verify(ldClient).boolVariation("another-flag", expectedContext, true);
    }
}
