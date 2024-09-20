package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.BEFORE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.ON_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.GLASGOW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.adjournedBeforeHearingDay;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.adjournedOnHearingDay;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isCaseUsingLocationRefData;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isIntegrated;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isPanelRequired;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isOnlyRemoteToRemoteHearingChannelUpdate;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.relistCaseImmediately;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryCircumstances.ENTRY_CLEARANCE_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isEntryClearanceDecision;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.BEFORE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.ON_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.GLASGOW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.adjournedBeforeHearingDay;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.adjournedOnHearingDay;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isCaseUsingLocationRefData;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isIntegrated;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isPanelRequired;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isOnlyRemoteToRemoteHearingChannelUpdate;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.relistCaseImmediately;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryCircumstances;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SourceOfAppeal;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class HandlerUtilsTest {
    private static final String ON_THE_PAPERS = "ONPPRS";

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
    private LocationBasedFeatureToggler locationBasedFeatureToggler;

    @Test
    void given_journey_type_aip_returns_true() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertTrue(HandlerUtils.isAipJourney(asylumCase));
    }

    @Test
    void given_journey_type_rep_returns_true() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        assertTrue(HandlerUtils.isRepJourney(asylumCase));
    }

    @Test
    void given_journey_type_legal_rep_returns_true() {
        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.of("Legal Rep Name"));
        assertTrue(HandlerUtils.isLegalRepJourney(asylumCase));
    }

    @Test
    void given_journey_type_legal_rep_returns_false() {
        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.empty());
        assertFalse(HandlerUtils.isLegalRepJourney(asylumCase));
    }

    @Test
    void no_journey_type_defaults_to_rep() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.empty());
        assertTrue(HandlerUtils.isRepJourney(asylumCase));
    }

    @Test
    void given_aip_journey_rep_test_should_fail() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.AIP));
        assertFalse(HandlerUtils.isRepJourney(asylumCase));
    }

    @Test
    void given_rep_journey_aip_test_should_fail() {
        when(asylumCase.read(JOURNEY_TYPE, JourneyType.class)).thenReturn(Optional.of(JourneyType.REP));
        assertFalse(HandlerUtils.isAipJourney(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = AppealType.class, names = {
        "RP", "PA", "EA", "HU", "DC"
    })
    void given_non_aaa_test_should_fail(AppealType appealType) {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(appealType));
        assertFalse(HandlerUtils.isAgeAssessmentAppeal(asylumCase));
    }

    @Test
    void given_aaa_test_should_pass() {
        when(asylumCase.read(APPEAL_TYPE, AppealType.class)).thenReturn(Optional.of(AppealType.AG));
        assertTrue(HandlerUtils.isAgeAssessmentAppeal(asylumCase));
    }

    @Test
    void isInternalCase_should_return_true() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(HandlerUtils.isInternalCase(asylumCase));
    }

    @Test
    void isInternalCase_should_return_false() {
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertFalse(HandlerUtils.isInternalCase(asylumCase));
    }

    @Test
    void sourceOfAppealEjp_should_return_true() {
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.TRANSFERRED_FROM_UPPER_TRIBUNAL));
        assertTrue(HandlerUtils.sourceOfAppealEjp(asylumCase));
    }

    @Test
    void sourceOfAppealEjp_should_return_false() {
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.PAPER_FORM));
        assertFalse(HandlerUtils.sourceOfAppealEjp(asylumCase));
    }

    @Test
    void isEjpCase_should_return_true() {
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(HandlerUtils.isEjpCase(asylumCase));
    }

    @Test
    void isEjpCase_should_return_false() {
        when(asylumCase.read(IS_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertFalse(HandlerUtils.isEjpCase(asylumCase));
    }

    @Test
    void isNotificationTurnedOff_should_return_true() {
        when(asylumCase.read(IS_NOTIFICATION_TURNED_OFF, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(HandlerUtils.isNotificationTurnedOff(asylumCase));
    }

    @Test
    void isNotificationTurnedOff_should_return_false() {
        when(asylumCase.read(IS_NOTIFICATION_TURNED_OFF, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertFalse(HandlerUtils.isNotificationTurnedOff(asylumCase));
    }

    @Test
    void isLegallyRepresentedEjpCase_should_return_true() {
        when(asylumCase.read(IS_LEGALLY_REPRESENTED_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        assertTrue(HandlerUtils.isLegallyRepresentedEjpCase(asylumCase));
    }

    @Test
    void isLegallyRepresentedEjpCase_should_return_false() {
        when(asylumCase.read(IS_LEGALLY_REPRESENTED_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        assertFalse(HandlerUtils.isLegallyRepresentedEjpCase(asylumCase));
    }

    @Test
    void get_appellant_full_name_should_return_appellant_display_name() {
        String appellantDisplayName = "FirstName FamilyName";
        when(asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class)).thenReturn(Optional.of(appellantDisplayName));

        assertEquals(appellantDisplayName, HandlerUtils.getAppellantFullName(asylumCase));
    }

    @Test
    void get_appellant_full_name_should_return_appellant_given_names_and_family_name() {
        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("FirstName SecondName"));
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("FamilyName"));

        assertEquals("FirstName SecondName FamilyName", HandlerUtils.getAppellantFullName(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"NO", "YES"})
    void should_check_set_value_in_auto_hearing_enabled_field(YesOrNo value) {
        when(locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase)).thenReturn(value);

        HandlerUtils.checkAndUpdateAutoHearingRequestEnabled(locationBasedFeatureToggler, asylumCase);

        verify(asylumCase, times(1)).write(
                AUTO_HEARING_REQUEST_ENABLED,
                value);
    }

    @Test
    void get_appellant_full_name_should_throw_exception() {
        assertThatThrownBy(() -> HandlerUtils.getAppellantFullName(asylumCase))
            .hasMessage("Appellant given names required")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.of("FirstName SecondName"));

        assertThatThrownBy(() -> HandlerUtils.getAppellantFullName(asylumCase))
            .hasMessage("Appellant family name required")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(asylumCase.read(APPELLANT_GIVEN_NAMES, String.class))
            .thenReturn(Optional.empty());
        when(asylumCase.read(APPELLANT_FAMILY_NAME, String.class))
            .thenReturn(Optional.of("FamilyName"));

        assertThatThrownBy(() -> HandlerUtils.getAppellantFullName(asylumCase))
            .hasMessage("Appellant given names required")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @CsvSource({
        "true, false, true, Active, RA0042, NO", // Only appellantLevelFlag RA0042 present
        "true, false, true, Active, PF0012, NO", // Only appellantLevelFlag PF0012 present
        "true, false, true, Active, PF0014, NO", // Only appellantLevelFlags PF0014 present
        "true, false, true, Active, PF0017, NO", // Only appellantLevelFlags PF0017 present
        "true, false, true, Active, PF0018, NO", // Only appellantLevelFlags PF0018 present
        "false, true, true, Active, CF0011, NO", // Only caseLevelFlags CF0011 present
        "true, true, true, Active, CF0011, NO", // All flags active and present
        "false, false, false, Inactive, PF0018, YES", // Only inactive flags present
        "false, false, false, Inactive, CF0011, YES", // No flags present
        "false, false, true, Active, CF0011, NO", // No flags present and Hearing is on the papers
    })
    void setDefaultAutoListHearingValue_ActiveFlagPresent(boolean hasAppellantFlags, boolean hasCaseFlags,
                                                          boolean isHearingOnThePaper, String active,
                                                          String flagCode, String expected) {
        when(asylumCase.read(HEARING_CHANNEL, DynamicList.class)).thenReturn(
            isHearingOnThePaper ?
                Optional.of(new DynamicList(new Value(ON_THE_PAPERS, "On the Papers"),
                    List.of(new Value(ON_THE_PAPERS, "On the Papers"))))
                : Optional.empty());

        List<CaseFlagDetail> existingFlags = List.of(
            new CaseFlagDetail("1",
                CaseFlagValue
                    .builder()
                    .flagCode(flagCode)
                    .name("flagName")
                    .status(active)
                    .build())
        );

        when(asylumCase.read(APPELLANT_LEVEL_FLAGS, StrategicCaseFlag.class)).thenReturn(
            hasAppellantFlags ? Optional.of(new StrategicCaseFlag("name", "role", existingFlags))
                : Optional.empty());

        when(asylumCase.read(CASE_LEVEL_FLAGS, StrategicCaseFlag.class)).thenReturn(
            hasCaseFlags ? Optional.of(new StrategicCaseFlag("name", "role", existingFlags))
                : Optional.empty());

        HandlerUtils.setDefaultAutoListHearingValue(asylumCase);

        verify(asylumCase, times(1)).write(AUTO_LIST_HEARING, YesOrNo.valueOf(expected));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES","NO"})
    void should_return_whether_panel_is_required(YesOrNo yesOrNo) {
        when(asylumCase.read(IS_PANEL_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        assertEquals(yesOrNo == YES, isPanelRequired(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES","NO"})
    void isIntegrated_should_work_as_expected(YesOrNo integrated) {
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(integrated));

        assertEquals(integrated == YES, isIntegrated(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES","NO"})
    void relistCaseImmediately_should_work_as_expected(YesOrNo relist) {
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class))
            .thenReturn(Optional.of(relist));

        assertEquals(relist == YES, relistCaseImmediately(asylumCase, false));
    }

    @Test
    void relistCaseImmediately_should_throw_exception() {

        assertThatThrownBy(() -> relistCaseImmediately(asylumCase, true))
            .hasMessage("Response to relist case immediately is not present")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = HearingAdjournmentDay.class, names = {"ON_HEARING_DATE","BEFORE_HEARING_DATE"})
    void adjournBeforeHearingDay_should_work_as_expected(HearingAdjournmentDay adjournmentDay) {
        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(adjournmentDay));

        assertEquals(adjournmentDay == BEFORE_HEARING_DATE, adjournedBeforeHearingDay(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = HearingAdjournmentDay.class, names = {"ON_HEARING_DATE","BEFORE_HEARING_DATE"})
    void adjournOnHearingDay_should_work_as_expected(HearingAdjournmentDay adjournmentDay) {
        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(adjournmentDay));

        assertEquals(adjournmentDay == ON_HEARING_DATE, adjournedOnHearingDay(asylumCase));
    }

    @Test
    public void read_json_file_list_valid_returns_list() throws IOException {
        String filePath = "/readJsonList.json";
        List<String> expectedCaseIdList = List.of("1234", "5678", "9012");
        List<String> result = HandlerUtils.readJsonFileList(filePath, "key");
        assertEquals(expectedCaseIdList, result);
    }

    @Test
    public void read_json_file_list_invalid_file_path_throws_io() {
        String filePath = "/missingCaseIdList.json";
        assertThrows(IOException.class, () -> {
            HandlerUtils.readJsonFileList(filePath, "key");
        });
    }

    @Test
    public void read_json_file_list_empty_list_returns_empty() throws IOException {
        String filePath = "/readJsonEmptyList.json";
        List<String> result = HandlerUtils.readJsonFileList(filePath, "key");
        assertEquals(new ArrayList<>(), result);
    }

    @Test
    public void read_json_file_list_non_array_json_returns_empty() throws IOException {
        String filePath = "/readJsonNonArray.json";
        List<String> result = HandlerUtils.readJsonFileList(filePath, "key");
        assertEquals(new ArrayList<>(), result);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES","NO"})
    void should_return_whether_case_uses_location_ref_data(YesOrNo yesOrNo) {
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        assertEquals(yesOrNo == YES, isCaseUsingLocationRefData(asylumCase));
    }

    @Test
    void setSelectedHearingCentreRefDataField() {
        HandlerUtils.setSelectedHearingCentreRefDataField(asylumCase, "hearingCentreLabel");

        verify(asylumCase, times(1)).write(
            SELECTED_HEARING_CENTRE_REF_DATA,
            "hearingCentreLabel");
    }

    @ParameterizedTest
    @CsvSource({
        "GLASGOW, GLASGOW, 01/02/2024, 01/02/2024, YES, YES, true",
        "GLASGOW, BIRMINGHAM, 01/02/2024, 03/02/2024, YES, YES, false",
        "GLASGOW, BIRMINGHAM, 01/02/2024, 01/02/2024, YES, YES, false",
        "GLASGOW, GLASGOW, 01/02/2024, 03/02/2024, YES, YES, false",
        "GLASGOW, GLASGOW, 01/02/2024, 01/02/2024, YES, NO, false",
        "GLASGOW, GLASGOW, 01/02/2024, 01/02/2024, NO, YES, false"
    })
    void test_isOnlyRemoteToRemoteHearingChannelUpdate(
        HearingCentre hearingCentreBefore,
        HearingCentre hearingCentre,
        String hearingDateBefore,
        String hearingDate,
        YesOrNo isRemoteBefore,
        YesOrNo isRemote,
        boolean expected) {

        when(asylumCaseBefore.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(hearingCentreBefore));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(hearingCentre));
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of(hearingDateBefore));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of(hearingDate));
        when(asylumCaseBefore.read(IS_REMOTE_HEARING, YesOrNo.class))
            .thenReturn(Optional.of(isRemoteBefore));
        when(asylumCase.read(IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(isRemote));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        assertEquals(expected, isOnlyRemoteToRemoteHearingChannelUpdate(callback));
    }

    @Test
    void isOnlyRemoteToRemoteHearingChannelUpdate_should_return_false_when_no_previous_case_data() {

        when(callback.getCaseDetailsBefore()).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(GLASGOW));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of("01/02/2024"));
        when(asylumCase.read(IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YES));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertFalse(isOnlyRemoteToRemoteHearingChannelUpdate(callback));
    }

    @ParameterizedTest
    @EnumSource(value = OutOfCountryDecisionType.class, names = { "REFUSAL_OF_PROTECTION", "REMOVAL_OF_CLIENT" })
    public void outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit_returns_false(OutOfCountryDecisionType type) {
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(type));

        assertFalse(outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = OutOfCountryDecisionType.class, names = { "REFUSAL_OF_HUMAN_RIGHTS", "REFUSE_PERMIT" })
    public void outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit_returns_true(OutOfCountryDecisionType type) {
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(type));

        assertTrue(outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase));
    }

    @Test
    public void outOfCountryCircumstances_returns_false() {
        when(asylumCase.read(OOC_APPEAL_ADMIN_J, OutOfCountryCircumstances.class)).thenReturn(Optional.of(ENTRY_CLEARANCE_DECISION));

        assertTrue(isEntryClearanceDecision(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = OutOfCountryCircumstances.class, names = { "LEAVE_UK", "NONE" })
    public void outOfCountryCircumstances_returns_true(OutOfCountryCircumstances outOfCountryCircumstances) {
        when(asylumCase.read(OOC_APPEAL_ADMIN_J, OutOfCountryCircumstances.class)).thenReturn(Optional.of(outOfCountryCircumstances));

        assertFalse(isEntryClearanceDecision(asylumCase));
    }
}
