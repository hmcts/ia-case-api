package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MARK_APPEAL_PAID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_HOME_OFFICE_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class HomeOfficeCaseValidatePreparerTest {

    private HomeOfficeCaseValidatePreparer homeOfficeCaseValidatePreparer;

    @Mock
    private FeatureToggler featureToggler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Mock
    private HomeOfficeApi<AsylumCase> homeOfficeApi;

    @BeforeEach
    public void setUp() {
        homeOfficeCaseValidatePreparer =
                new HomeOfficeCaseValidatePreparer(true, featureToggler, homeOfficeApi);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void handle_should_return_error_if_appeal_type_not_present() {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(SUBMIT_APPEAL);
        when(homeOfficeApi.aboutToStart(callback)).thenReturn(asylumCase);

        assertThatThrownBy(() -> homeOfficeCaseValidatePreparer.handle(ABOUT_TO_START, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("AppealType is not present.");
    }



    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = { "PA", "RP", "DC", "EA", "HU", "AG" })
    void handle_should_return_error_for_ejp_appeals(AppealType appealType) {

        when(callback.getEvent()).thenReturn(Event.REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> response =
            homeOfficeCaseValidatePreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getErrors()).contains("You cannot request Home Office data for this appeal");
    }

    @Test
    void handle_should_return_error_for_aaa_appeals() {

        when(callback.getEvent()).thenReturn(Event.REQUEST_HOME_OFFICE_DATA);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));

        PreSubmitCallbackResponse<AsylumCase> response =
            homeOfficeCaseValidatePreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getErrors()).contains("You cannot request Home Office data for this appeal");
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void handler_checks_home_office_integration_enabled_returns_yes_and_uan_feature_enabled_pa_rp_appeal_types(Event event, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(homeOfficeApi.aboutToStart(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response =
            homeOfficeCaseValidatePreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();

        if (Arrays.asList(PA, RP).contains(appealType)) {
            verify(homeOfficeApi, times(1)).aboutToStart(callback);
            verify(asylumCase, times(1)).write(
                    IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);
        } else {
            verify(homeOfficeApi, times(0)).aboutToStart(callback);
            verify(asylumCase, times(0)).write(
                    IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);
        }
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void handler_should_invoke_homeoffice_api_for_detained_appeals(Event event, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(homeOfficeApi.aboutToStart(callback)).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> response =
            homeOfficeCaseValidatePreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();

        if (Arrays.asList(PA, RP).contains(appealType)) {
            verify(homeOfficeApi, times(1)).aboutToStart(callback);
            verify(asylumCase, times(1)).write(
                IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);
        } else {
            verify(homeOfficeApi, times(0)).aboutToStart(callback);
            verify(asylumCase, times(0)).write(
                IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);
        }
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void handler_should_not_invoke_homeoffice_api_for_ejp(Event event, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(homeOfficeApi.aboutToStart(callback)).thenReturn(asylumCase);
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        homeOfficeCaseValidatePreparer.handle(ABOUT_TO_START, callback);

        verify(homeOfficeApi, times(0)).aboutToStart(callback);
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void handler_should_not_invoke_homeoffice_api_when_isNotificationTurnedOff_yes(Event event, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(homeOfficeApi.aboutToStart(callback)).thenReturn(asylumCase);
        when(asylumCase.read(IS_NOTIFICATION_TURNED_OFF, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        homeOfficeCaseValidatePreparer.handle(ABOUT_TO_START, callback);

        verify(homeOfficeApi, times(0)).aboutToStart(callback);
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void handler_checks_home_office_integration_enabled_returns_yes_and_uan_feature_enabled_dc_ea_hu_appeal_types(Event event, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(homeOfficeApi.aboutToStart(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response =
                homeOfficeCaseValidatePreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();

        if (Arrays.asList(DC, EA, HU).contains(appealType)) {
            verify(homeOfficeApi, times(1)).aboutToStart(callback);
            verify(asylumCase, times(1)).write(
                    IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);
        } else {
            verify(homeOfficeApi, times(0)).aboutToStart(callback);
            verify(asylumCase, times(0)).write(
                    IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);
        }
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void handler_checks_home_office_integration_enabled_returns_yes_and_uan_feature_enabled(Event event, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(homeOfficeApi.aboutToStart(callback)).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> response =
                homeOfficeCaseValidatePreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();

        verify(homeOfficeApi, times(1)).aboutToStart(callback);
        verify(asylumCase, times(1)).write(
                IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);
    }

    private static Stream<Arguments> eventAndAppealTypesData() {

        return Stream.of(
                Arguments.of(SUBMIT_APPEAL, PA),
                Arguments.of(SUBMIT_APPEAL, RP),
                Arguments.of(SUBMIT_APPEAL, DC),
                Arguments.of(SUBMIT_APPEAL, EA),
                Arguments.of(SUBMIT_APPEAL, HU),
                Arguments.of(MARK_APPEAL_PAID, PA),
                Arguments.of(MARK_APPEAL_PAID, RP),
                Arguments.of(MARK_APPEAL_PAID, DC),
                Arguments.of(MARK_APPEAL_PAID, EA),
                Arguments.of(MARK_APPEAL_PAID, HU),
                Arguments.of(REQUEST_HOME_OFFICE_DATA, PA),
                Arguments.of(REQUEST_HOME_OFFICE_DATA, RP),
                Arguments.of(REQUEST_HOME_OFFICE_DATA, DC),
                Arguments.of(REQUEST_HOME_OFFICE_DATA, EA),
                Arguments.of(REQUEST_HOME_OFFICE_DATA, HU)
        );
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void handler_checks_home_office_integration_enabled_returns_yes_and_uan_feature_disabled(Event event, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(false);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));

        PreSubmitCallbackResponse<AsylumCase> response =
                homeOfficeCaseValidatePreparer.handle(ABOUT_TO_START, callback);

        assertThat(response).isNotNull();
        assertThat(response.getData()).isNotEmpty();
        assertThat(response.getData()).isEqualTo(asylumCase);
        assertThat(response.getErrors()).isEmpty();
        verify(asylumCase, times(1)).write(
                IS_HOME_OFFICE_INTEGRATION_ENABLED, YesOrNo.YES);
        verify(homeOfficeApi, times(0)).aboutToStart(callback);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = homeOfficeCaseValidatePreparer.canHandle(callbackStage, callback);

                if (callbackStage == ABOUT_TO_START
                    && (callback.getEvent() == SUBMIT_APPEAL
                    || callback.getEvent() == MARK_APPEAL_PAID
                    || callback.getEvent() == REQUEST_HOME_OFFICE_DATA)
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

        assertThatThrownBy(() -> homeOfficeCaseValidatePreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeCaseValidatePreparer.canHandle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeCaseValidatePreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeCaseValidatePreparer.handle(ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> homeOfficeCaseValidatePreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(START_APPEAL);
        assertThatThrownBy(() -> homeOfficeCaseValidatePreparer.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

    }

}
