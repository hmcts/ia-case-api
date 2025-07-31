package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.DC;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.RP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.GLASGOW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.*;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HomeOfficeApi;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class HomeOfficeCaseNotificationsHandlerTest {

    @Mock
    private HomeOfficeApi<AsylumCase> homeOfficeApi;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AsylumCase asylumCaseBefore;
    @Mock
    private FeatureToggler featureToggler;

    private HomeOfficeCaseNotificationsHandler homeOfficeCaseNotificationsHandler;

    private IdValue originalDirection8 = new IdValue(
        "8",
        new Direction("explanation8", Parties.LEGAL_REPRESENTATIVE, "2020-01-02",
            "2020-01-01", DirectionTag.NONE, Collections.emptyList(),
            Collections.emptyList(),
            UUID.randomUUID().toString(),
            "directionType8"
        )
    );

    private IdValue originalDirection9 = new IdValue(
        "9",
        new Direction("explanation9", Parties.RESPONDENT, "2020-01-02",
            "2020-01-01", DirectionTag.NONE, Collections.emptyList(),
            Collections.emptyList(),
            UUID.randomUUID().toString(),
            "directionType9"
        )
    );

    private IdValue originalDirection10 = new IdValue(
        "10",
        new Direction("explanation10", Parties.LEGAL_REPRESENTATIVE, "2020-01-02",
            "2020-01-01", DirectionTag.RESPONDENT_REVIEW, Collections.emptyList(),
            Collections.emptyList(),
            UUID.randomUUID().toString(),
            "directionType10"
        )
    );

    private IdValue originalDirection11 = new IdValue(
        "11",
        new Direction("explanation11", Parties.RESPONDENT, "2020-01-02",
            "2020-01-01", DirectionTag.NONE, Collections.emptyList(),
            Collections.emptyList(),
            UUID.randomUUID().toString(),
            "directionType11"
        )
    );

    @BeforeEach
    void setUp() {
        homeOfficeCaseNotificationsHandler =
            new HomeOfficeCaseNotificationsHandler(featureToggler, homeOfficeApi);
        when(featureToggler.getValue("home-office-notification-feature", false)).thenReturn(true);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void handle_should_error_if_appeal_type_is_not_present() {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(LIST_CASE);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("AppealType is not present.");
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void should_call_home_office_api_and_update_the_case_for_pa_rp_appeal_types(Event event, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(event);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        if (Arrays.asList(PA, RP).contains(appealType)) {
            verify(homeOfficeApi, times(1)).aboutToSubmit(callback);
        } else {
            verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
        }
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void should_call_home_office_api_and_update_the_case_for_dc_ea_hu_appeal_types(Event event, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(event);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(1)).aboutToSubmit(callback);
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void should_call_home_office_api_and_update_the_case_for_all_appeal_types(Event event, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);

        when(callback.getEvent()).thenReturn(event);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));


        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        if (Arrays.asList(DC, EA, HU).contains(appealType)) {
            verify(homeOfficeApi, times(1)).aboutToSubmit(callback);
        } else {
            verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
        }
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void should_not_call_home_office_api_when_validation_unsuccessful(Event event, AppealType appealType) {

        when(callback.getEvent()).thenReturn(event);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("FAIL"));
        when(asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
    }

    @ParameterizedTest
    @MethodSource("eventAndAppealTypesData")
    void should_not_call_home_office_api_for_in_progress_case(Event event, AppealType appealType) {

        when(callback.getEvent()).thenReturn(event);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.empty());
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
    }

    private static Stream<Arguments> eventAndAppealTypesData() {

        return Stream.of(
            Arguments.of(REQUEST_RESPONDENT_EVIDENCE, PA),
            Arguments.of(REQUEST_RESPONDENT_EVIDENCE, RP),
            Arguments.of(REQUEST_RESPONDENT_EVIDENCE, DC),
            Arguments.of(REQUEST_RESPONDENT_EVIDENCE, EA),
            Arguments.of(REQUEST_RESPONDENT_EVIDENCE, HU),
            Arguments.of(REQUEST_RESPONDENT_REVIEW, PA),
            Arguments.of(REQUEST_RESPONDENT_REVIEW, RP),
            Arguments.of(REQUEST_RESPONDENT_REVIEW, DC),
            Arguments.of(REQUEST_RESPONDENT_REVIEW, EA),
            Arguments.of(REQUEST_RESPONDENT_REVIEW, HU),
            Arguments.of(LIST_CASE, PA),
            Arguments.of(LIST_CASE, RP),
            Arguments.of(LIST_CASE, DC),
            Arguments.of(LIST_CASE, EA),
            Arguments.of(LIST_CASE, HU),
            Arguments.of(EDIT_CASE_LISTING, PA),
            Arguments.of(EDIT_CASE_LISTING, RP),
            Arguments.of(EDIT_CASE_LISTING, DC),
            Arguments.of(EDIT_CASE_LISTING, EA),
            Arguments.of(EDIT_CASE_LISTING, HU),
            Arguments.of(ADJOURN_HEARING_WITHOUT_DATE, PA),
            Arguments.of(ADJOURN_HEARING_WITHOUT_DATE, RP),
            Arguments.of(ADJOURN_HEARING_WITHOUT_DATE, DC),
            Arguments.of(ADJOURN_HEARING_WITHOUT_DATE, EA),
            Arguments.of(ADJOURN_HEARING_WITHOUT_DATE, HU),
            Arguments.of(SEND_DECISION_AND_REASONS, PA),
            Arguments.of(SEND_DECISION_AND_REASONS, RP),
            Arguments.of(SEND_DECISION_AND_REASONS, DC),
            Arguments.of(SEND_DECISION_AND_REASONS, EA),
            Arguments.of(SEND_DECISION_AND_REASONS, HU),
            Arguments.of(APPLY_FOR_FTPA_APPELLANT, PA),
            Arguments.of(APPLY_FOR_FTPA_APPELLANT, RP),
            Arguments.of(APPLY_FOR_FTPA_APPELLANT, DC),
            Arguments.of(APPLY_FOR_FTPA_APPELLANT, EA),
            Arguments.of(APPLY_FOR_FTPA_APPELLANT, HU),
            Arguments.of(APPLY_FOR_FTPA_RESPONDENT, PA),
            Arguments.of(APPLY_FOR_FTPA_RESPONDENT, RP),
            Arguments.of(APPLY_FOR_FTPA_RESPONDENT, DC),
            Arguments.of(APPLY_FOR_FTPA_RESPONDENT, EA),
            Arguments.of(APPLY_FOR_FTPA_RESPONDENT, HU),
            Arguments.of(LEADERSHIP_JUDGE_FTPA_DECISION, PA),
            Arguments.of(LEADERSHIP_JUDGE_FTPA_DECISION, RP),
            Arguments.of(LEADERSHIP_JUDGE_FTPA_DECISION, DC),
            Arguments.of(LEADERSHIP_JUDGE_FTPA_DECISION, EA),
            Arguments.of(LEADERSHIP_JUDGE_FTPA_DECISION, HU),
            Arguments.of(RESIDENT_JUDGE_FTPA_DECISION, PA),
            Arguments.of(RESIDENT_JUDGE_FTPA_DECISION, RP),
            Arguments.of(RESIDENT_JUDGE_FTPA_DECISION, DC),
            Arguments.of(RESIDENT_JUDGE_FTPA_DECISION, EA),
            Arguments.of(RESIDENT_JUDGE_FTPA_DECISION, HU),
            Arguments.of(DECIDE_FTPA_APPLICATION, PA),
            Arguments.of(DECIDE_FTPA_APPLICATION, RP),
            Arguments.of(DECIDE_FTPA_APPLICATION, DC),
            Arguments.of(DECIDE_FTPA_APPLICATION, EA),
            Arguments.of(DECIDE_FTPA_APPLICATION, HU),
            Arguments.of(END_APPEAL, PA),
            Arguments.of(END_APPEAL, RP),
            Arguments.of(END_APPEAL, DC),
            Arguments.of(END_APPEAL, EA),
            Arguments.of(END_APPEAL, HU)
        );
    }

    @ParameterizedTest
    @MethodSource("hoFlagAndAppealTypesData")
    void should_not_call_home_office_api_when_ooc_and_human_rights_decision_is_chosen(boolean hoUanfeatureFlag, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-flag", false)).thenReturn(hoUanfeatureFlag);
        when(callback.getEvent()).thenReturn(REQUEST_RESPONDENT_EVIDENCE);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        if (hoUanfeatureFlag) {
            when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        } else {
            when(homeOfficeApi.call(callback)).thenReturn(asylumCase);
        }

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        if (hoUanfeatureFlag) {
            verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
        } else {
            verify(homeOfficeApi, times(0)).call(callback);

        }
    }

    private static Stream<Arguments> hoFlagAndAppealTypesData() {
        return Stream.of(
            Arguments.of(true, PA),
            Arguments.of(true, RP),
            Arguments.of(true, DC),
            Arguments.of(true, EA),
            Arguments.of(true, HU),
            Arguments.of(false, PA),
            Arguments.of(false, RP),
            Arguments.of(false, DC),
            Arguments.of(false, EA),
            Arguments.of(false, HU)
        );
    }

    @ParameterizedTest
    @MethodSource("stateAndAppealTypesData")
    void should_call_home_office_api_and_update_the_case_for_direction_due_date_pa_rp_appeal_types(State state, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(CHANGE_DIRECTION_DUE_DATE);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getState()).thenReturn(state);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)).thenReturn(Optional.of(Parties.RESPONDENT));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        if (Arrays.asList(PA, RP).contains(appealType)) {

            verify(homeOfficeApi, times(1)).aboutToSubmit(callback);
        } else {

            verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
        }
    }

    @ParameterizedTest
    @MethodSource("stateAndAppealTypesData")
    void should_call_home_office_api_and_update_the_case_for_direction_due_date_dc_ea_hu_appeal_types(State state, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(CHANGE_DIRECTION_DUE_DATE);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getState()).thenReturn(state);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)).thenReturn(Optional.of(Parties.RESPONDENT));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        if (Arrays.asList(DC, EA, HU).contains(appealType)) {

            verify(homeOfficeApi, times(1)).aboutToSubmit(callback);
        } else {

            verify(homeOfficeApi, times(0)).aboutToSubmit(callback);
        }
    }

    @ParameterizedTest
    @MethodSource("stateAndAppealTypesData")
    void should_call_home_office_api_and_update_the_case_for_direction_due_date(State state, AppealType appealType) {

        when(featureToggler.getValue("home-office-uan-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-pa-rp-feature", false)).thenReturn(true);
        when(featureToggler.getValue("home-office-uan-dc-ea-hu-feature", false)).thenReturn(true);
        when(callback.getEvent()).thenReturn(CHANGE_DIRECTION_DUE_DATE);
        when(homeOfficeApi.aboutToSubmit(callback)).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getState()).thenReturn(state);
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        when(asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)).thenReturn(Optional.of(Parties.RESPONDENT));
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(HOME_OFFICE_SEARCH_STATUS, String.class)).thenReturn(Optional.of("SUCCESS"));
        when(asylumCase.read(HOME_OFFICE_NOTIFICATIONS_ELIGIBLE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(homeOfficeApi, times(1)).aboutToSubmit(callback);
    }

    private static Stream<Arguments> stateAndAppealTypesData() {

        return Stream.of(
            Arguments.of(RESPONDENT_REVIEW, PA),
            Arguments.of(RESPONDENT_REVIEW, RP),
            Arguments.of(RESPONDENT_REVIEW, DC),
            Arguments.of(RESPONDENT_REVIEW, EA),
            Arguments.of(RESPONDENT_REVIEW, HU),
            Arguments.of(AWAITING_RESPONDENT_EVIDENCE, PA),
            Arguments.of(AWAITING_RESPONDENT_EVIDENCE, RP),
            Arguments.of(AWAITING_RESPONDENT_EVIDENCE, DC),
            Arguments.of(AWAITING_RESPONDENT_EVIDENCE, EA),
            Arguments.of(AWAITING_RESPONDENT_EVIDENCE, HU)
        );
    }

    @Test
    void should_return_true_for_respondent_direction() {

        when(asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)).thenReturn(Optional.of(Parties.RESPONDENT));
        assertTrue(homeOfficeCaseNotificationsHandler.isDirectionForRespondentParties(asylumCase));
    }

    @Test
    void should_return_false_for_non_respondent_direction() {

        when(asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)).thenReturn(Optional.of(Parties.LEGAL_REPRESENTATIVE));
        assertFalse(homeOfficeCaseNotificationsHandler.isDirectionForRespondentParties(asylumCase));
    }

    @Test
    void should_return_error_for_missing_direction() {

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.isDirectionForRespondentParties(asylumCase))
            .hasMessage("sendDirectionParties is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_return_right_direction_for_multiple_send_direction() {

        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(originalDirection8);
        directionList.add(originalDirection9);
        directionList.add(originalDirection10);
        directionList.add(originalDirection11);
        when(asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS)).thenReturn(Optional.of((directionList)));

        Optional<Direction> selectedDirection = homeOfficeCaseNotificationsHandler.getLatestNonStandardRespondentDirection(asylumCase);

        assertTrue(selectedDirection.isPresent());
        assertEquals(Parties.RESPONDENT, selectedDirection.get().getParties());
        assertEquals("explanation11", selectedDirection.get().getExplanation());
    }

    @Test
    void should_return_empty_direction_for_invalid_send_direction() {

        List<IdValue<Direction>> directionList = new ArrayList<>();
        directionList.add(originalDirection9);
        directionList.add(originalDirection10);
        when(asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS)).thenReturn(Optional.of((directionList)));

        Optional<Direction> selectedDirection = homeOfficeCaseNotificationsHandler.getLatestNonStandardRespondentDirection(asylumCase);

        assertTrue(selectedDirection.isEmpty());
    }

    @ParameterizedTest
    @EnumSource(Event.class)
    void cannot_handle_if_invalid_callback_stage(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        for (State state : State.values()) {
            when(callback.getCaseDetails().getState()).thenReturn(state);
            List<PreSubmitCallbackStage> invalidCallbackStages = Arrays.stream(PreSubmitCallbackStage.values())
                .filter(callbackStage -> callbackStage != ABOUT_TO_SUBMIT).toList();
            for (PreSubmitCallbackStage callbackStage : invalidCallbackStages) {
                assertFalse(homeOfficeCaseNotificationsHandler.canHandle(callbackStage, callback));
            }
        }
    }

    @ParameterizedTest
    @EnumSource(State.class)
    void it_can_handle_callback_change_direction_due_date(State state) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(CHANGE_DIRECTION_DUE_DATE);
        when(callback.getCaseDetails().getState()).thenReturn(state);
        when(asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class)).thenReturn(Optional.of(Parties.RESPONDENT));

        boolean canHandle = homeOfficeCaseNotificationsHandler.canHandle(ABOUT_TO_SUBMIT, callback);
        if (List.of(AWAITING_RESPONDENT_EVIDENCE, RESPONDENT_REVIEW).contains(state)) {
            assertTrue(canHandle);
        } else {
            assertFalse(canHandle);
        }
    }

    @ParameterizedTest
    @EnumSource(State.class)
    void it_can_handle_callback_send_direction(State state) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(SEND_DIRECTION);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails().getState()).thenReturn(state);
        when(asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class))
            .thenReturn(Optional.of(Parties.RESPONDENT));
        when(asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS))
            .thenReturn(Optional.of(List.of(originalDirection8, originalDirection9)));

        boolean canHandle = homeOfficeCaseNotificationsHandler.canHandle(ABOUT_TO_SUBMIT, callback);
        if (state.equals(AWAITING_RESPONDENT_EVIDENCE)) {
            assertTrue(canHandle);
        } else {
            assertFalse(canHandle);
        }
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"SEND_DIRECTION", "CHANGE_DIRECTION_DUE_DATE"}, mode = EnumSource.Mode.EXCLUDE)
    void it_can_handle_callback(Event event) {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(event);
        List<Event> validEvents = List.of(
            REQUEST_RESPONDENT_EVIDENCE,
            REQUEST_RESPONDENT_REVIEW,
            LIST_CASE,
            EDIT_CASE_LISTING,
            ADJOURN_HEARING_WITHOUT_DATE,
            SEND_DECISION_AND_REASONS,
            APPLY_FOR_FTPA_APPELLANT,
            APPLY_FOR_FTPA_RESPONDENT,
            LEADERSHIP_JUDGE_FTPA_DECISION,
            RESIDENT_JUDGE_FTPA_DECISION,
            DECIDE_FTPA_APPLICATION,
            END_APPEAL,
            REQUEST_RESPONSE_AMEND);
        for (State state : State.values()) {
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(callback.getCaseDetails().getState()).thenReturn(state);
            when(asylumCase.read(DIRECTION_EDIT_PARTIES, Parties.class))
                .thenReturn(Optional.of(Parties.RESPONDENT));
            when(asylumCase.read(AsylumCaseFieldDefinition.DIRECTIONS))
                .thenReturn(Optional.of(Collections.singletonList(originalDirection8)));

            boolean canHandle = homeOfficeCaseNotificationsHandler.canHandle(ABOUT_TO_SUBMIT, callback);

            if (validEvents.contains(event)) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
        }
        reset(callback);
    }

    @Test
    void it_cannot_handle_if_edit_case_listing_for_remote_to_remote_hearing_channel_update() {

        when(asylumCaseBefore.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(GLASGOW));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(GLASGOW));
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of("01/02/2024"));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of("01/02/2024"));
        when(asylumCaseBefore.read(IS_REMOTE_HEARING, YesOrNo.class))
            .thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YES));

        when(callback.getEvent()).thenReturn(EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            assertFalse(homeOfficeCaseNotificationsHandler.canHandle(callbackStage, callback));
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.canHandle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.handle(ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(START_APPEAL);
        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handler_throws_error_if_feature_not_enabled() {

        homeOfficeCaseNotificationsHandler = new HomeOfficeCaseNotificationsHandler(
            featureToggler,
            homeOfficeApi
        );

        assertThatThrownBy(() -> homeOfficeCaseNotificationsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
