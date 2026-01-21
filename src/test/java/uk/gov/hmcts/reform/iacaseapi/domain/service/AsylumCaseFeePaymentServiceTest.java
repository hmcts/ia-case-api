package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CallbackApiDelegator;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AsylumCaseFeePaymentServiceTest {

    private static final String ENDPOINT = "http://endpoint";
    private static final String ABOUT_TO_START_PATH = "/asylum/ccdAboutToStart";
    private static final String ABOUT_TO_SUBMIT_PATH = "/asylum/ccdAboutToSubmit";

    @Mock
    private CallbackApiDelegator callbackApiDelegator;
    @Mock
    private Callback<AsylumCase> callback;

    private AsylumCaseFeePaymentService asylumCaseFeeApiPayment;

    @BeforeEach
    public void setUp() {

        asylumCaseFeeApiPayment =
            new AsylumCaseFeePaymentService(
                callbackApiDelegator,
                ENDPOINT,
                ABOUT_TO_SUBMIT_PATH,
                ABOUT_TO_START_PATH
            );
    }

    @Test
    void should_delegate_callback_to_downstream_api() {

        final AsylumCase feePaymentAsylumCase = mock(AsylumCase.class);

        when(callbackApiDelegator.delegate(callback, ENDPOINT + ABOUT_TO_SUBMIT_PATH))
            .thenReturn(feePaymentAsylumCase);

        final AsylumCase actualAsylumCase = asylumCaseFeeApiPayment.aboutToSubmit(callback);

        verify(callbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + ABOUT_TO_SUBMIT_PATH);

        assertEquals(feePaymentAsylumCase, actualAsylumCase);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> asylumCaseFeeApiPayment.aboutToSubmit(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
