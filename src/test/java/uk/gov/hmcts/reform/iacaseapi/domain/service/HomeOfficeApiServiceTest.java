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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CallbackApiDelegator;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HomeOfficeApiServiceTest {

    private static final String ENDPOINT = "some-endpoint";
    private static final String ABOUT_TO_START_PATH = "some-path";
    private static final String ABOUT_TO_SUBMIT_PATH = "some-path";

    @Mock
    private CallbackApiDelegator callbackApiDelegator;
    @Mock
    private Callback<AsylumCase> callback;

    private HomeOfficeApiService homeOfficeApiService;

    @BeforeEach
    public void setUp() {

        homeOfficeApiService =
            new HomeOfficeApiService(
                callbackApiDelegator,
                ENDPOINT,
                ABOUT_TO_START_PATH,
                ABOUT_TO_SUBMIT_PATH
            );
    }

    @ParameterizedTest
    @ValueSource(strings = { ABOUT_TO_SUBMIT_PATH })
    void should_delegate_callback_to_downstream_call_api_and_get_response(String path) {

        final AsylumCase asylumCaseWithHomeOfficeData = mock(AsylumCase.class);

        when(callbackApiDelegator.delegate(callback, ENDPOINT + path))
                .thenReturn(asylumCaseWithHomeOfficeData);

        final AsylumCase actualAsylumCase = homeOfficeApiService.call(callback);

        verify(callbackApiDelegator, times(1))
                .delegate(callback, ENDPOINT + path);

        assertEquals(asylumCaseWithHomeOfficeData, actualAsylumCase);
    }

    @ParameterizedTest
    @ValueSource(strings = { ABOUT_TO_START_PATH, ABOUT_TO_SUBMIT_PATH })
    void should_delegate_callback_to_downstream_api_and_get_response(String path) {

        final AsylumCase asylumCaseWithHomeOfficeData = mock(AsylumCase.class);

        when(callbackApiDelegator.delegate(callback, ENDPOINT + path))
            .thenReturn(asylumCaseWithHomeOfficeData);

        final AsylumCase actualAsylumCase = path.equals(ABOUT_TO_START_PATH)
                ? homeOfficeApiService.aboutToStart(callback) : homeOfficeApiService.aboutToSubmit(callback);

        verify(callbackApiDelegator, times(1))
            .delegate(callback, ENDPOINT + path);

        assertEquals(asylumCaseWithHomeOfficeData, actualAsylumCase);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> homeOfficeApiService.aboutToSubmit(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = { ABOUT_TO_START_PATH, ABOUT_TO_SUBMIT_PATH })
    void should_handle_error_from_downstream_api(String path) {

        when(callbackApiDelegator.delegate(callback, ENDPOINT + path))
            .thenThrow(new RequiredFieldMissingException("Home office reference number is a required field"));

        assertThatThrownBy(() -> homeOfficeApiService.aboutToSubmit(callback))
            .hasMessage("Home office reference number is a required field")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

}
