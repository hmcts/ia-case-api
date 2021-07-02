package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.HomeOfficeCaseStatus;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HomeOfficeRequestHomeOfficeDataPreparerTest {

    private static final String HOME_OFFICE_DATA_PRESENT_MESSAGE = "The Home Office data "
        + "has already been retrieved successfully "
        + "and is available in the validation tab.";

    private HomeOfficeRequestHomeOfficeDataPreparer homeOfficeDataPreparer;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private HomeOfficeCaseStatus homeOfficeCaseStatus;

    @BeforeEach
    public void setUp() {
        homeOfficeDataPreparer =
            new HomeOfficeRequestHomeOfficeDataPreparer(true);
    }

    @Test
    void handler_checks_existing_home_office_data_returns_error_if_exists() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class))
            .thenReturn(Optional.of(homeOfficeCaseStatus));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class))
            .thenReturn(Optional.of("SUCCESS"));

        PreSubmitCallbackResponse<AsylumCase> response =
            homeOfficeDataPreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).containsExactly(HOME_OFFICE_DATA_PRESENT_MESSAGE);
        assertTrue(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class).isPresent());

    }

    @Test
    void handler_should_not_error_for_no_match() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class))
                .thenReturn(Optional.of(homeOfficeCaseStatus));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class))
                .thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(HOME_OFFICE_SEARCH_NO_MATCH, String.class)).thenReturn(Optional.of("NO_MATCH"));

        PreSubmitCallbackResponse<AsylumCase> response =
                homeOfficeDataPreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
    }

    @Test
    void handler_checks_existing_home_office_case_data_returns_no_error_for_empty_case() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class))
            .thenReturn(Optional.of("SUCCESS"));

        PreSubmitCallbackResponse<AsylumCase> response =
            homeOfficeDataPreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
        assertFalse(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class).isPresent());

    }

    @Test
    void handler_checks_existing_home_office_data_returns_no_error_for_fail_status() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class))
            .thenReturn(Optional.of(homeOfficeCaseStatus));

        PreSubmitCallbackResponse<AsylumCase> response =
            homeOfficeDataPreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
        assertTrue(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class).isPresent());

    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = homeOfficeDataPreparer.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_START
                    && callback.getEvent() == REQUEST_HOME_OFFICE_DATA
                ) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> homeOfficeDataPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeDataPreparer.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeDataPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeDataPreparer.handle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> homeOfficeDataPreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        assertThatThrownBy(() -> homeOfficeDataPreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }

    @Test
    void handler_throws_error_if_feature_not_enabled() {

        homeOfficeDataPreparer = new HomeOfficeRequestHomeOfficeDataPreparer(false);

        assertThatThrownBy(() -> homeOfficeDataPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }

}
