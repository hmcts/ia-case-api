package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.LDUser;
import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdentityManagerResponseException;


@ExtendWith(MockitoExtension.class)
class LaunchDarklyFeatureTogglerTest {

    @Mock
    private LDClientInterface ldClient;

    @Mock
    private UserDetails userDetails;

    @Mock
    private LDValue expectedJsonValue;

    @InjectMocks
    private LaunchDarklyFeatureToggler launchDarklyFeatureToggler;

    @Test
    void should_return_default_value_when_key_does_not_exist() {
        String notExistingKey = "not-existing-key";
        when(userDetails.getId()).thenReturn("id");
        when(userDetails.getForename()).thenReturn("forname");
        when(userDetails.getSurname()).thenReturn("surname");
        when(userDetails.getEmailAddress()).thenReturn("emailAddress");

        when(ldClient.boolVariation(
            notExistingKey,
            LDContext.fromUser(new LDUser.Builder(userDetails.getId())
                .firstName(userDetails.getForename())
                .lastName(userDetails.getSurname())
                .email(userDetails.getEmailAddress())
                .build()),
            true)
        ).thenReturn(true);

        assertTrue(launchDarklyFeatureToggler.getValue(notExistingKey, true));
    }

    @Test
    void should_return_value_when_key_exists() {
        String existingKey = "existing-key";
        when(userDetails.getId()).thenReturn("id");
        when(userDetails.getForename()).thenReturn("forname");
        when(userDetails.getSurname()).thenReturn("surname");
        when(userDetails.getEmailAddress()).thenReturn("emailAddress");
        when(ldClient.boolVariation(
            existingKey,
            LDContext.fromUser(new LDUser.Builder(userDetails.getId())
                .firstName(userDetails.getForename())
                .lastName(userDetails.getSurname())
                .email(userDetails.getEmailAddress())
                .build()),
            false)
        ).thenReturn(true);

        assertTrue(launchDarklyFeatureToggler.getValue(existingKey, false));
    }

    @Test
    void should_return_json_value_when_key_exists() {
        String existingKey = "existing-key";
        when(userDetails.getId()).thenReturn("id");
        when(userDetails.getForename()).thenReturn("forname");
        when(userDetails.getSurname()).thenReturn("surname");
        when(userDetails.getEmailAddress()).thenReturn("emailAddress");
        when(ldClient.jsonValueVariation(
            existingKey,
            LDContext.fromUser(new LDUser.Builder(userDetails.getId())
                .firstName(userDetails.getForename())
                .lastName(userDetails.getSurname())
                .email(userDetails.getEmailAddress())
                .build()),
            LDValue.ofNull())
        ).thenReturn(expectedJsonValue);

        assertEquals(expectedJsonValue, launchDarklyFeatureToggler.getJsonValue(existingKey, LDValue.ofNull()));
    }

    @Test
    void throw_exception_when_user_details_provider_unavailable() {
        when(userDetails.getId()).thenThrow(IdentityManagerResponseException.class);

        assertThatThrownBy(() -> launchDarklyFeatureToggler.getValue("existing-key", true))
            .isExactlyInstanceOf(IdentityManagerResponseException.class);
    }
}
