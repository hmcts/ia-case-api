package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.ConfigValidatorAppListener.CLUSTER_NAME;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class ConfigValidatorAppListenerTest {

    @Mock
    private Environment env;

    private ConfigValidatorAppListener configValidatorAppListener;

    @BeforeEach
    void setup() {
        configValidatorAppListener = new ConfigValidatorAppListener(env);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "cft-preview-01-aks, ",
        "cft-preview-01-aks, null"
    }, nullValues = {"null"})
    void throwsExceptionWhenIaConfigValidatorSecretIsEmptySimulateCluster(String clusterName, String secret) {
        // Given
        configValidatorAppListener.setIaConfigValidatorSecret(secret);
        when(env.getProperty(CLUSTER_NAME)).thenReturn(clusterName);

        // When
        assertThrows(IllegalArgumentException.class, configValidatorAppListener::breakOnMissingIaConfigValidatorSecret);

        // Then
        verify(env).getProperty(CLUSTER_NAME);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "cft-preview-01-aks, secret",
        "null, secret",
        "null, ",
        "null, null"
    }, nullValues = {"null"})
    void runsSuccessfullyWhenIaConfigValidatorSecretsClusterNameAreCorrectlySetSimulateBoth(String clusterName, String secret) {
        // Given
        configValidatorAppListener.setIaConfigValidatorSecret(secret);
        when(env.getProperty(CLUSTER_NAME)).thenReturn(clusterName);

        // When
        configValidatorAppListener.breakOnMissingIaConfigValidatorSecret();

        // Then
        verify(env).getProperty(CLUSTER_NAME);
    }
}
