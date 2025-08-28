package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.BEFORE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingAdjournmentDay.ON_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.GLASGOW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption.WILL_PAY_FOR_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryCircumstances.ENTRY_CLEARANCE_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.HELP_WITH_FEES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.HO_WAIVER_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.NO_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.*;

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
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void should_return_whether_panel_is_required(YesOrNo yesOrNo) {
        when(asylumCase.read(IS_PANEL_REQUIRED, YesOrNo.class)).thenReturn(Optional.of(yesOrNo));

        assertEquals(yesOrNo == YES, isPanelRequired(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
    void isIntegrated_should_work_as_expected(YesOrNo integrated) {
        when(asylumCase.read(IS_INTEGRATED, YesOrNo.class)).thenReturn(Optional.of(integrated));

        assertEquals(integrated == YES, isIntegrated(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
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
    @EnumSource(value = HearingAdjournmentDay.class, names = {"ON_HEARING_DATE", "BEFORE_HEARING_DATE"})
    void adjournBeforeHearingDay_should_work_as_expected(HearingAdjournmentDay adjournmentDay) {
        when(asylumCase.read(HEARING_ADJOURNMENT_WHEN, HearingAdjournmentDay.class))
            .thenReturn(Optional.of(adjournmentDay));

        assertEquals(adjournmentDay == BEFORE_HEARING_DATE, adjournedBeforeHearingDay(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = HearingAdjournmentDay.class, names = {"ON_HEARING_DATE", "BEFORE_HEARING_DATE"})
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
    @EnumSource(value = YesOrNo.class, names = {"YES", "NO"})
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
    @EnumSource(value = RemissionType.class, names = {"NO_REMISSION", "HO_WAIVER_REMISSION", "HELP_WITH_FEES", "EXCEPTIONAL_CIRCUMSTANCES_REMISSION"})
    void should_return_is_non_aip_remission_exists_proper_value(RemissionType remissionType) {

        if (NO_REMISSION.equals(remissionType)) {
            assertFalse(HandlerUtils.isRemissionExists(Optional.of(remissionType)));
        } else {
            assertTrue(HandlerUtils.isRemissionExists(Optional.of(remissionType)));
        }
    }

    @ParameterizedTest
    @MethodSource("provideAipRemissionParameters")
    void should_return_aip_remission_exists_proper_value(RemissionOption remissionOption, HelpWithFeesOption helpWithFeesOption, boolean isDlrmFeeRemissionFlag, boolean expectedResult) {
        boolean actualResult = HandlerUtils.isRemissionExistsAip(Optional.of(remissionOption), Optional.of(helpWithFeesOption), isDlrmFeeRemissionFlag);
        assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> provideAipRemissionParameters() {
        return Stream.of(
            Arguments.of(RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE, HelpWithFeesOption.WANT_TO_APPLY, true, true),
            Arguments.of(RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE, HelpWithFeesOption.WANT_TO_APPLY, false, false),
            Arguments.of(RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE, HelpWithFeesOption.ALREADY_APPLIED, true, true),
            Arguments.of(RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE, HelpWithFeesOption.WILL_PAY_FOR_APPEAL, true, true),

            Arguments.of(RemissionOption.FEE_WAIVER_FROM_HOME_OFFICE, HelpWithFeesOption.WANT_TO_APPLY, true, true),
            Arguments.of(RemissionOption.FEE_WAIVER_FROM_HOME_OFFICE, HelpWithFeesOption.ALREADY_APPLIED, true, true),
            Arguments.of(RemissionOption.FEE_WAIVER_FROM_HOME_OFFICE, HelpWithFeesOption.WILL_PAY_FOR_APPEAL, true, true),

            Arguments.of(RemissionOption.UNDER_18_GET_SUPPORT, HelpWithFeesOption.WANT_TO_APPLY, true, true),
            Arguments.of(RemissionOption.UNDER_18_GET_SUPPORT, HelpWithFeesOption.ALREADY_APPLIED, true, true),
            Arguments.of(RemissionOption.UNDER_18_GET_SUPPORT, HelpWithFeesOption.WILL_PAY_FOR_APPEAL, true, true),

            Arguments.of(RemissionOption.PARENT_GET_SUPPORT, HelpWithFeesOption.WANT_TO_APPLY, true, true),
            Arguments.of(RemissionOption.PARENT_GET_SUPPORT, HelpWithFeesOption.ALREADY_APPLIED, true, true),
            Arguments.of(RemissionOption.PARENT_GET_SUPPORT, HelpWithFeesOption.WILL_PAY_FOR_APPEAL, true, true),

            Arguments.of(RemissionOption.NO_REMISSION, HelpWithFeesOption.WANT_TO_APPLY, true, true),
            Arguments.of(RemissionOption.NO_REMISSION, HelpWithFeesOption.ALREADY_APPLIED, true, true),
            Arguments.of(RemissionOption.NO_REMISSION, HelpWithFeesOption.WILL_PAY_FOR_APPEAL, true, false),

            Arguments.of(RemissionOption.I_WANT_TO_GET_HELP_WITH_FEES, HelpWithFeesOption.WANT_TO_APPLY, true, true),
            Arguments.of(RemissionOption.I_WANT_TO_GET_HELP_WITH_FEES, HelpWithFeesOption.ALREADY_APPLIED, true, true),
            Arguments.of(RemissionOption.I_WANT_TO_GET_HELP_WITH_FEES, HelpWithFeesOption.WILL_PAY_FOR_APPEAL, true, true)
        );
    }

    @ParameterizedTest
    @EnumSource(value = OutOfCountryDecisionType.class, names = {"REFUSAL_OF_PROTECTION", "REMOVAL_OF_CLIENT"})
    public void outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit_returns_false(OutOfCountryDecisionType type) {
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class)).thenReturn(Optional.of(type));

        assertFalse(outOfCountryDecisionTypeIsRefusalOfHumanRightsOrPermit(asylumCase));
    }

    @ParameterizedTest
    @EnumSource(value = OutOfCountryDecisionType.class, names = {"REFUSAL_OF_HUMAN_RIGHTS", "REFUSE_PERMIT"})
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
    @EnumSource(value = OutOfCountryCircumstances.class, names = {"LEAVE_UK", "NONE"})
    public void outOfCountryCircumstances_returns_true(OutOfCountryCircumstances outOfCountryCircumstances) {
        when(asylumCase.read(OOC_APPEAL_ADMIN_J, OutOfCountryCircumstances.class)).thenReturn(Optional.of(outOfCountryCircumstances));

        assertFalse(isEntryClearanceDecision(asylumCase));
    }

    @Test
    void clearRequestRemissionFields_should_clear_all_fields() {
        HandlerUtils.clearRequestRemissionFields(asylumCase);

        verify(asylumCase).clear(LATE_REMISSION_TYPE);
        verify(asylumCase).clear(REMISSION_CLAIM);
        verify(asylumCase).clear(ASYLUM_SUPPORT_REFERENCE);
        verify(asylumCase).clear(ASYLUM_SUPPORT_DOCUMENT);
        verify(asylumCase).clear(LEGAL_AID_ACCOUNT_NUMBER);
        verify(asylumCase).clear(SECTION17_DOCUMENT);
        verify(asylumCase).clear(SECTION20_DOCUMENT);
        verify(asylumCase).clear(HOME_OFFICE_WAIVER_DOCUMENT);
        verify(asylumCase).clear(HELP_WITH_FEES_REFERENCE_NUMBER);
        verify(asylumCase).clear(EXCEPTIONAL_CIRCUMSTANCES);
        verify(asylumCase).clear(REMISSION_EC_EVIDENCE_DOCUMENTS);
    }

    @Test
    void clearPreviousRemissionCaseFields_should_clear_fields_for_HO_WAIVER_REMISSION_and_asylumSupport() {
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class)).thenReturn(Optional.of("asylumSupport"));

        HandlerUtils.clearPreviousRemissionCaseFields(asylumCase);

        asylumCase.clear(LEGAL_AID_ACCOUNT_NUMBER);
        asylumCase.clear(SECTION17_DOCUMENT);
        asylumCase.clear(SECTION20_DOCUMENT);
        asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
        asylumCase.clear(HOME_OFFICE_WAIVER_DOCUMENT);
        asylumCase.clear(HELP_WITH_FEES_REFERENCE_NUMBER);
        asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
        asylumCase.clear(EXCEPTIONAL_CIRCUMSTANCES);
        asylumCase.clear(REMISSION_EC_EVIDENCE_DOCUMENTS);
        verify(asylumCase).clear(REMISSION_DECISION);
        verify(asylumCase).clear(AMOUNT_REMITTED);
        verify(asylumCase).clear(AMOUNT_LEFT_TO_PAY);
        verify(asylumCase).clear(REMISSION_DECISION_REASON);
    }


    @Test
    void clearPreviousRemissionCaseFields_should_clear_fields_for_HO_WAIVER_REMISSION_and_legalAid() {
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class)).thenReturn(Optional.of("legalAid"));

        HandlerUtils.clearPreviousRemissionCaseFields(asylumCase);

        asylumCase.clear(ASYLUM_SUPPORT_REFERENCE);
        asylumCase.clear(ASYLUM_SUPPORT_DOCUMENT);
        asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
        asylumCase.clear(SECTION17_DOCUMENT);
        asylumCase.clear(SECTION20_DOCUMENT);
        asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
        asylumCase.clear(HOME_OFFICE_WAIVER_DOCUMENT);
        asylumCase.clear(HELP_WITH_FEES_REFERENCE_NUMBER);
        asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
        asylumCase.clear(EXCEPTIONAL_CIRCUMSTANCES);
        asylumCase.clear(REMISSION_EC_EVIDENCE_DOCUMENTS);
        verify(asylumCase).clear(REMISSION_DECISION);
        verify(asylumCase).clear(AMOUNT_REMITTED);
        verify(asylumCase).clear(AMOUNT_LEFT_TO_PAY);
        verify(asylumCase).clear(REMISSION_DECISION_REASON);
    }


    @Test
    void clearPreviousRemissionCaseFields_should_clear_fields_for_HO_WAIVER_REMISSION_and_section17() {
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class)).thenReturn(Optional.of("section17"));

        HandlerUtils.clearPreviousRemissionCaseFields(asylumCase);

        asylumCase.clear(ASYLUM_SUPPORT_REFERENCE);
        asylumCase.clear(ASYLUM_SUPPORT_DOCUMENT);
        asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
        asylumCase.clear(LEGAL_AID_ACCOUNT_NUMBER);
        asylumCase.clear(SECTION20_DOCUMENT);
        asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
        asylumCase.clear(HOME_OFFICE_WAIVER_DOCUMENT);
        asylumCase.clear(HELP_WITH_FEES_REFERENCE_NUMBER);
        asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
        asylumCase.clear(EXCEPTIONAL_CIRCUMSTANCES);
        asylumCase.clear(REMISSION_EC_EVIDENCE_DOCUMENTS);
        verify(asylumCase).clear(REMISSION_DECISION);
        verify(asylumCase).clear(AMOUNT_REMITTED);
        verify(asylumCase).clear(AMOUNT_LEFT_TO_PAY);
        verify(asylumCase).clear(REMISSION_DECISION_REASON);
    }


    @Test
    void clearPreviousRemissionCaseFields_should_clear_fields_for_HO_WAIVER_REMISSION_and_section20() {
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class)).thenReturn(Optional.of("section20"));

        HandlerUtils.clearPreviousRemissionCaseFields(asylumCase);

        asylumCase.clear(ASYLUM_SUPPORT_REFERENCE);
        asylumCase.clear(ASYLUM_SUPPORT_DOCUMENT);
        asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
        asylumCase.clear(LEGAL_AID_ACCOUNT_NUMBER);
        asylumCase.clear(SECTION17_DOCUMENT);
        asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
        asylumCase.clear(HOME_OFFICE_WAIVER_DOCUMENT);
        asylumCase.clear(HELP_WITH_FEES_REFERENCE_NUMBER);
        asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
        asylumCase.clear(EXCEPTIONAL_CIRCUMSTANCES);
        asylumCase.clear(REMISSION_EC_EVIDENCE_DOCUMENTS);
        verify(asylumCase).clear(REMISSION_DECISION);
        verify(asylumCase).clear(AMOUNT_REMITTED);
        verify(asylumCase).clear(AMOUNT_LEFT_TO_PAY);
        verify(asylumCase).clear(REMISSION_DECISION_REASON);
    }


    @Test
    void clearPreviousRemissionCaseFields_should_clear_fields_for_HO_WAIVER_REMISSION_and_homeOfficeWaiver() {
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(RemissionType.HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class)).thenReturn(Optional.of("homeOfficeWaiver"));

        HandlerUtils.clearPreviousRemissionCaseFields(asylumCase);

        asylumCase.clear(ASYLUM_SUPPORT_REFERENCE);
        asylumCase.clear(ASYLUM_SUPPORT_DOCUMENT);
        asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
        asylumCase.clear(LEGAL_AID_ACCOUNT_NUMBER);
        asylumCase.clear(SECTION17_DOCUMENT);
        asylumCase.clear(SECTION20_DOCUMENT);
        asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
        asylumCase.clear(HELP_WITH_FEES_REFERENCE_NUMBER);
        asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
        asylumCase.clear(EXCEPTIONAL_CIRCUMSTANCES);
        asylumCase.clear(REMISSION_EC_EVIDENCE_DOCUMENTS);
        verify(asylumCase).clear(REMISSION_DECISION);
        verify(asylumCase).clear(AMOUNT_REMITTED);
        verify(asylumCase).clear(AMOUNT_LEFT_TO_PAY);
        verify(asylumCase).clear(REMISSION_DECISION_REASON);
    }


    @Test
    void clearPreviousRemissionCaseFields_should_clear_fields_for_HELP_WITH_FEES() {
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(HELP_WITH_FEES));

        HandlerUtils.clearPreviousRemissionCaseFields(asylumCase);

        asylumCase.clear(ASYLUM_SUPPORT_REFERENCE);
        asylumCase.clear(ASYLUM_SUPPORT_DOCUMENT);
        asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
        asylumCase.clear(LEGAL_AID_ACCOUNT_NUMBER);
        asylumCase.clear(SECTION17_DOCUMENT);
        asylumCase.clear(SECTION20_DOCUMENT);
        asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
        asylumCase.clear(HOME_OFFICE_WAIVER_DOCUMENT);
        asylumCase.clear(EXCEPTIONAL_CIRCUMSTANCES);
        asylumCase.clear(REMISSION_EC_EVIDENCE_DOCUMENTS);
        verify(asylumCase).clear(REMISSION_DECISION);
        verify(asylumCase).clear(AMOUNT_REMITTED);
        verify(asylumCase).clear(AMOUNT_LEFT_TO_PAY);
        verify(asylumCase).clear(REMISSION_DECISION_REASON);
    }


    @Test
    void clearPreviousRemissionCaseFields_should_clear_fields_for_EXCEPTIONAL_CIRCUMSTANCES_REMISSION() {
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION));

        HandlerUtils.clearPreviousRemissionCaseFields(asylumCase);

        asylumCase.clear(ASYLUM_SUPPORT_REFERENCE);
        asylumCase.clear(ASYLUM_SUPPORT_DOCUMENT);
        asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
        asylumCase.clear(LEGAL_AID_ACCOUNT_NUMBER);
        asylumCase.clear(SECTION17_DOCUMENT);
        asylumCase.clear(SECTION20_DOCUMENT);
        asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
        asylumCase.clear(HOME_OFFICE_WAIVER_DOCUMENT);
        asylumCase.clear(HELP_WITH_FEES_REFERENCE_NUMBER);
        asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
        verify(asylumCase).clear(REMISSION_DECISION);
        verify(asylumCase).clear(AMOUNT_REMITTED);
        verify(asylumCase).clear(AMOUNT_LEFT_TO_PAY);
        verify(asylumCase).clear(REMISSION_DECISION_REASON);
    }


    @Test
    void clearPreviousRemissionCaseFields_should_not_clear_if_lateRemissionType_not_present() {
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.empty());
        HandlerUtils.clearPreviousRemissionCaseFields(asylumCase);
        verify(asylumCase, never()).clear(any());
    }

    @Test
    void clearPreviousRemissionCaseFields_should_only_clear_previous_fields_if_HO_WAIVER_REMISSION_and_invalid() {
        when(asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(HO_WAIVER_REMISSION));
        when(asylumCase.read(REMISSION_CLAIM, String.class)).thenReturn(Optional.of("invalid"));
        HandlerUtils.clearPreviousRemissionCaseFields(asylumCase);
        verify(asylumCase).clear(REMISSION_DECISION);
        verify(asylumCase).clear(AMOUNT_REMITTED);
        verify(asylumCase).clear(AMOUNT_LEFT_TO_PAY);
        verify(asylumCase).clear(REMISSION_DECISION_REASON);
    }

    private static Stream<Arguments> appealHasRemissionOptionOrTypeTrue() {
        return Stream.of(
            Arguments.of(Optional.of(RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE), Optional.empty(), Optional.empty(), Optional.empty()),
            Arguments.of(Optional.empty(), Optional.of(HelpWithFeesOption.WANT_TO_APPLY), Optional.empty(), Optional.empty()),
            Arguments.of(Optional.empty(), Optional.empty(), Optional.of(RemissionType.HO_WAIVER_REMISSION), Optional.empty()),
            Arguments.of(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(HELP_WITH_FEES)),
            Arguments.of(Optional.of(RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE),
                Optional.of(HelpWithFeesOption.WANT_TO_APPLY),
                Optional.of(RemissionType.HO_WAIVER_REMISSION),
                Optional.of(HELP_WITH_FEES)
            )
        );
    }

    private static Stream<Arguments> appealHasRemissionOptionOrTypeFalse() {
        return Stream.of(
            Arguments.of(Optional.of(RemissionOption.NO_REMISSION), Optional.empty(), Optional.empty(), Optional.empty()),
            Arguments.of(Optional.empty(), Optional.of(WILL_PAY_FOR_APPEAL), Optional.empty(), Optional.empty()),
            Arguments.of(Optional.empty(), Optional.empty(), Optional.of(RemissionType.NO_REMISSION), Optional.empty()),
            Arguments.of(Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(RemissionType.NO_REMISSION)),
            Arguments.of(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty())
        );
    }

    @ParameterizedTest
    @MethodSource("appealHasRemissionOptionOrTypeTrue")
    void appealHasRemissionOptionOrTypeMethod_returns_true(Optional<RemissionOption> remissionOption,
                                                           Optional<HelpWithFeesOption> helpWithFeesOption,
                                                           Optional<RemissionType> remissionType,
                                                           Optional<RemissionType> lateRemissionType) {
        assertTrue(appealHasRemissionOptionOrType(remissionOption, helpWithFeesOption, remissionType, lateRemissionType));
    }

    @ParameterizedTest
    @MethodSource("appealHasRemissionOptionOrTypeFalse")
    void appealHasRemissionOptionOrTypeMethod_returns_false(Optional<RemissionOption> remissionOption,
                                                            Optional<HelpWithFeesOption> helpWithFeesOption,
                                                            Optional<RemissionType> remissionType,
                                                            Optional<RemissionType> lateRemissionType) {
        assertFalse(appealHasRemissionOptionOrType(remissionOption, helpWithFeesOption, remissionType, lateRemissionType));
    }

    private static Stream<Arguments> remissionTypeAndClaimProvider() {
        return Stream.of(
            Arguments.of(HO_WAIVER_REMISSION, "asylumSupport", FeeRemissionType.ASYLUM_SUPPORT),
            Arguments.of(HO_WAIVER_REMISSION, "legalAid", FeeRemissionType.LEGAL_AID),
            Arguments.of(HO_WAIVER_REMISSION, "section17", FeeRemissionType.SECTION_17),
            Arguments.of(HO_WAIVER_REMISSION, "section20", FeeRemissionType.SECTION_20),
            Arguments.of(HO_WAIVER_REMISSION, "homeOfficeWaiver", FeeRemissionType.HO_WAIVER),
            Arguments.of(HO_WAIVER_REMISSION, "unknown", null), // Unknown claim
            Arguments.of(HELP_WITH_FEES, "", FeeRemissionType.HELP_WITH_FEES),
            Arguments.of(EXCEPTIONAL_CIRCUMSTANCES_REMISSION, "", FeeRemissionType.EXCEPTIONAL_CIRCUMSTANCES)
        );
    }

    @ParameterizedTest
    @MethodSource("remissionTypeAndClaimProvider")
    void should_set_fee_remission_type_details_correctly(RemissionType remissionType, String remissionClaim, String expectedFeeRemissionType) {
        when(asylumCase.read(AsylumCaseFieldDefinition.LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.of(remissionType));
        when(asylumCase.read(AsylumCaseFieldDefinition.REMISSION_CLAIM, String.class)).thenReturn(Optional.of(remissionClaim));

        HandlerUtils.setFeeRemissionTypeDetails(asylumCase);
        verify(asylumCase).write(AsylumCaseFieldDefinition.REMISSION_TYPE, remissionType);
        verify(asylumCase, expectedFeeRemissionType != null ? times(1) : never())
            .write(AsylumCaseFieldDefinition.FEE_REMISSION_TYPE, expectedFeeRemissionType);
    }

    @Test
    void should_not_set_fee_remission_type_details_if_late_remission_type_not_present() {
        when(asylumCase.read(AsylumCaseFieldDefinition.LATE_REMISSION_TYPE, RemissionType.class)).thenReturn(Optional.empty());
        HandlerUtils.setFeeRemissionTypeDetails(asylumCase);
        verify(asylumCase, never()).write(eq(AsylumCaseFieldDefinition.REMISSION_TYPE), any());
        verify(asylumCase, never()).write(eq(AsylumCaseFieldDefinition.FEE_REMISSION_TYPE), any());
    }
}
