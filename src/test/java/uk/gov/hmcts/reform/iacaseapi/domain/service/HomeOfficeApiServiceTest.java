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
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseCallbackApiDelegator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HomeOfficeApiServiceTest {

    private static final String ENDPOINT = "some-endpoint";
    private static final String ABOUT_TO_SUBMIT_PATH = "some-path";

    @Mock
    private AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;
    @Mock
    private Callback<AsylumCase> callback;

    private HomeOfficeApiService homeOfficeApiService;

    @BeforeEach
    public void setUp() {

        homeOfficeApiService =
            new HomeOfficeApiService(
                asylumCaseCallbackApiDelegator,
                ENDPOINT,
                ABOUT_TO_SUBMIT_PATH
            );
    }

    @Test
    void should_delegate_callback_to_downstream_api_and_get_response() {

        final AsylumCase asylumCaseWithHomeOfficeData = mock(AsylumCase.class);

        when(asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT + ABOUT_TO_SUBMIT_PATH))
            .thenReturn(asylumCaseWithHomeOfficeData);

        final AsylumCase actualAsylumCase = homeOfficeApiService.call(callback);

        verify(asylumCaseCallbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + ABOUT_TO_SUBMIT_PATH);

        assertEquals(asylumCaseWithHomeOfficeData, actualAsylumCase);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> homeOfficeApiService.call(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_handle_error_from_downstream_api() {

        when(asylumCaseCallbackApiDelegator.delegate(callback, ENDPOINT + ABOUT_TO_SUBMIT_PATH))
            .thenThrow(new RequiredFieldMissingException("Home office reference number is a required field"));

        assertThatThrownBy(() -> homeOfficeApiService.call(callback))
            .hasMessage("Home office reference number is a required field")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

}
