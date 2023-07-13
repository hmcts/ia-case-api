
package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ConfigValidatorAppListenerTest {

    @Test
    void throwsExceptionWhenIaConfigValidatorSecretIsNull() {
        // Given
        ConfigValidatorAppListener configValidatorAppListener = new ConfigValidatorAppListener();
        configValidatorAppListener.setIaConfigValidatorSecret(null);

        // When/Then
        assertThrows(IllegalArgumentException.class, configValidatorAppListener::breakOnMissingIaConfigValidatorSecret);
    }

    @Test
    void throwsExceptionWhenIaConfigValidatorSecretIsEmpty() {
        // Given
        ConfigValidatorAppListener configValidatorAppListener = new ConfigValidatorAppListener();
        configValidatorAppListener.setIaConfigValidatorSecret("");

        // When/Then
        assertThrows(IllegalArgumentException.class, configValidatorAppListener::breakOnMissingIaConfigValidatorSecret);
    }

    @Test
    @SuppressWarnings("java:S2699") // suppressing SonarLint warning on assertions as it's ok for this test not to have any
    void runsSuccessfullyWhenIaConfigValidatorSecretsAreCorrectlySet() {
        // Given
        ConfigValidatorAppListener configValidatorAppListener = new ConfigValidatorAppListener();
        configValidatorAppListener.setIaConfigValidatorSecret("secret");

        // When
        configValidatorAppListener.breakOnMissingIaConfigValidatorSecret();

        // Then
        // I run successfully till the end
    }

}
