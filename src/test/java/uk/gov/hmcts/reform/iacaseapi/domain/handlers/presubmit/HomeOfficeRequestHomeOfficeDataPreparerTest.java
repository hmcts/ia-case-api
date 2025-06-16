package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice.HomeOfficeCaseStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class HomeOfficeRequestHomeOfficeDataPreparerTest {

    private HomeOfficeRequestHomeOfficeDataPreparer homeOfficeDataPreparer;

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private HomeOfficeCaseStatus homeOfficeCaseStatus;
    @Mock
    private FeatureToggler featureToggler;

    @BeforeEach
    public void setUp() {
        homeOfficeDataPreparer =
            new HomeOfficeRequestHomeOfficeDataPreparer(true, featureToggler);
    }

    @Test
    void handle_should_return_error_if_appeal_type_not_present() {

        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> homeOfficeDataPreparer.handle(ABOUT_TO_START, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("AppealType is not present.");
    }



    @Test
    void handle_should_return_error_for_aaa_appeals() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));

        PreSubmitCallbackResponse<AsylumCase> response =
            homeOfficeDataPreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getErrors()).contains("You cannot request Home Office data for this appeal");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU" })
    void handler_should_not_error_for_no_match(AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
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

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU" })
    void handler_checks_existing_home_office_case_data_returns_no_error_for_empty_case(AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
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

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU" })
    void handler_checks_existing_home_office_data_returns_no_error_for_fail_status(AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
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

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU" })
    void handle_should_return_error_for_out_of_country_appeals(AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class))
                .thenReturn(Optional.of(homeOfficeCaseStatus));

        PreSubmitCallbackResponse<AsylumCase> response =
                homeOfficeDataPreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isNotEmpty();
        assertThat(response.getErrors()).contains("You cannot request Home Office data for an out of country appeal");
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU" })
    void handle_should_return_error_for_appeal_types_not_enabled(AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(Event.REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(HOME_OFFICE_CASE_STATUS_DATA, HomeOfficeCaseStatus.class))
                .thenReturn(Optional.of(homeOfficeCaseStatus));

        PreSubmitCallbackResponse<AsylumCase> response =
                homeOfficeDataPreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);

        if (Arrays.asList(DC, EA, HU).contains(appealType)) {

            assertThat(response.getErrors()).isNotEmpty();
            assertThat(response.getErrors()).contains("You can only request Home Office data for an appeal against a "
                    + "Protection or Revocation of Protection decision");
        }
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

        homeOfficeDataPreparer = new HomeOfficeRequestHomeOfficeDataPreparer(false, featureToggler);

        assertThatThrownBy(() -> homeOfficeDataPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }

}
