package uk.gov.hmcts.reform.bailcaseapi.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.ConfigValidatorAppListener;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.ConfigValidatorAppListener.CLUSTER_NAME;

@ExtendWith(MockitoExtension.class)
class ConfigValidatorAppListenerTest {

    @Mock
    private Environment env;

    private ConfigValidatorAppListener configValidatorAppListener;

    @BeforeEach
    public void setup() {
        configValidatorAppListener = new ConfigValidatorAppListener();
        configValidatorAppListener.setEnvironment(env);
    }

    @Test
    @SuppressWarnings("java:S2699")
    void throwsExceptionWhenIaConfigValidatorSecretIsNullSimulateLocal() {
        // Given
        configValidatorAppListener.setIaConfigValidatorSecret(null);
        when(env.getProperty(CLUSTER_NAME)).thenReturn(null);

        // When
        configValidatorAppListener.breakOnMissingIaConfigValidatorSecret();

        // Then
        verify(env).getProperty(CLUSTER_NAME);
    }

    @Test
    void throwsExceptionWhenIaConfigValidatorSecretIsNullSimulateCluster() {
        // Given
        configValidatorAppListener.setIaConfigValidatorSecret(null);
        when(env.getProperty(CLUSTER_NAME)).thenReturn("cft-preview-01-aks");

        // When/Then
        assertThrows(IllegalArgumentException.class, configValidatorAppListener::breakOnMissingIaConfigValidatorSecret);
        verify(env).getProperty(CLUSTER_NAME);
    }

    @Test
    void throwsExceptionWhenIaConfigValidatorSecretIsEmptySimulateLocal() {
        // Given
        configValidatorAppListener.setIaConfigValidatorSecret("");
        when(env.getProperty(CLUSTER_NAME)).thenReturn(null);

        // When
        configValidatorAppListener.breakOnMissingIaConfigValidatorSecret();

        // Then
        verify(env).getProperty(CLUSTER_NAME);
    }

    @Test
    void throwsExceptionWhenIaConfigValidatorSecretIsEmptySimulateCluster() {
        // Given
        configValidatorAppListener.setIaConfigValidatorSecret("");
        when(env.getProperty(CLUSTER_NAME)).thenReturn("cft-preview-01-aks");


        // When/Then
        assertThrows(IllegalArgumentException.class, configValidatorAppListener::breakOnMissingIaConfigValidatorSecret);
        verify(env).getProperty(CLUSTER_NAME);
    }

    // suppressing SonarLint warning on assertions as it's ok for this test not to have any
    @Test
    @SuppressWarnings("java:S2699")
    void runsSuccessfullyWhenIaConfigValidatorSecretsAreCorrectlySetSimulateLocal() {
        // Given
        configValidatorAppListener.setIaConfigValidatorSecret("secret");
        when(env.getProperty(CLUSTER_NAME)).thenReturn(null);

        // When
        configValidatorAppListener.breakOnMissingIaConfigValidatorSecret();

        // Then
        verify(env).getProperty(CLUSTER_NAME);
        // I run successfully till the end
    }

    // suppressing SonarLint warning on assertions as it's ok for this test not to have any
    @Test
    @SuppressWarnings("java:S2699")
    void runsSuccessfullyWhenIaConfigValidatorSecretsAreCorrectlySetSimulateCluster() {
        // Given
        configValidatorAppListener.setIaConfigValidatorSecret("secret");
        when(env.getProperty(CLUSTER_NAME)).thenReturn("cft-preview-01-aks");

        // When
        configValidatorAppListener.breakOnMissingIaConfigValidatorSecret();

        // Then
        verify(env).getProperty(CLUSTER_NAME);
        // I run successfully till the end
    }

}
