package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.launchdarkly.sdk.LDContext;
import com.launchdarkly.sdk.LDValue;
import com.launchdarkly.sdk.server.interfaces.LDClientInterface;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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

    @Captor
    private ArgumentCaptor<LDContext> ldContextCaptor;

    @InjectMocks
    private LaunchDarklyFeatureToggler launchDarklyFeatureToggler;

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void should_return_default_value_when_key_does_not_exist(boolean defaultValue) {
        when(userDetails.getId()).thenReturn("id");
        when(userDetails.getForename()).thenReturn("forname");
        when(userDetails.getSurname()).thenReturn("surname");
        when(userDetails.getEmailAddress()).thenReturn("emailAddress");
        when(ldClient.boolVariation(anyString(), any(LDContext.class), anyBoolean())).thenReturn(defaultValue);

        assertEquals(defaultValue, launchDarklyFeatureToggler.getValue("not-existing-key", defaultValue));
        verify(ldClient).boolVariation(eq("not-existing-key"), ldContextCaptor.capture(), eq(defaultValue));
        assertLdContext(ldContextCaptor.getValue());
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void should_return_value_when_key_exists(boolean defaultValue) {
        when(userDetails.getId()).thenReturn("id");
        when(userDetails.getForename()).thenReturn("forname");
        when(userDetails.getSurname()).thenReturn("surname");
        when(userDetails.getEmailAddress()).thenReturn("emailAddress");
        when(ldClient.boolVariation(anyString(), any(LDContext.class), anyBoolean())).thenReturn(true);

        assertTrue(launchDarklyFeatureToggler.getValue("existing-key", defaultValue));
        verify(ldClient).boolVariation(eq("existing-key"), ldContextCaptor.capture(), eq(defaultValue));
        assertLdContext(ldContextCaptor.getValue());
    }

    @Test
    void should_return_json_value_when_key_exists() {
        when(userDetails.getId()).thenReturn("id");
        when(userDetails.getForename()).thenReturn("forname");
        when(userDetails.getSurname()).thenReturn("surname");
        when(userDetails.getEmailAddress()).thenReturn("emailAddress");
        when(ldClient.jsonValueVariation(anyString(), any(LDContext.class), any(LDValue.class)))
            .thenReturn(expectedJsonValue);

        assertEquals(expectedJsonValue, launchDarklyFeatureToggler.getJsonValue("existing-key",
            expectedJsonValue));
        verify(ldClient).jsonValueVariation(eq("existing-key"), ldContextCaptor.capture(), eq(expectedJsonValue));
        assertLdContext(ldContextCaptor.getValue());
    }

    @Test
    void throw_exception_when_user_details_provider_unavailable() {
        when(userDetails.getId()).thenThrow(IdentityManagerResponseException.class);

        assertThatThrownBy(() -> launchDarklyFeatureToggler.getValue("existing-key", true))
            .isExactlyInstanceOf(IdentityManagerResponseException.class);
    }

    private void assertLdContext(LDContext ldContext) {
        assertEquals("id", ldContext.getKey());
        assertEquals("forname", ldContext.getValue("firstName").stringValue());
        assertEquals("surname", ldContext.getValue("lastName").stringValue());
        assertEquals("emailAddress", ldContext.getValue("email").stringValue());
    }
}
