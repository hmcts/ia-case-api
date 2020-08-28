package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseCallbackApiDelegator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class AsylumCaseFeePaymentServiceTest {

    static final String ENDPOINT = "http://endpoint";
    static final String ABOUT_TO_START_PATH = "/asylum/ccdAboutToStart";
    static final String ABOUT_TO_SUBMIT_PATH = "/asylum/ccdAboutToSubmit";

    @Mock private AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;
    @Mock private Callback<AsylumCase> callback;

    AsylumCaseFeePaymentService asylumCaseFeeApiPayment;

    @BeforeEach
    void setUp() {

        asylumCaseFeeApiPayment =
            new AsylumCaseFeePaymentService(
                asylumCaseCallbackApiDelegator,
                ENDPOINT,
                ABOUT_TO_SUBMIT_PATH,
                ABOUT_TO_START_PATH
            );
    }

    @Test
    void should_delegate_callback_to_downstream_api() {

        final AsylumCase feePaymentAsylumCase = mock(AsylumCase.class);

        when(asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT + ABOUT_TO_SUBMIT_PATH))
            .thenReturn(feePaymentAsylumCase);

        final AsylumCase actualAsylumCase = asylumCaseFeeApiPayment.aboutToSubmit(callback);

        verify(asylumCaseCallbackApiDelegator, times(1))
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
