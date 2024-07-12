package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SIGN_LANGUAGE_INTERPRETER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageCategory.SPOKEN_LANGUAGE_INTERPRETER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.InterpreterLanguageRefData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SourceOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("unchecked")
class HandlerUtilsTest {

    @Mock
    private AsylumCase asylumCase;
    @Mock
    private InterpreterLanguage interpreterLanguage1;
    @Mock
    private InterpreterLanguage interpreterLanguage2;
    @Mock
    private InterpreterLanguage interpreterLanguage3;

    private static final String signLanguage1 = "Sign language - Aaa";
    private static final String signLanguage2 = "Sign language - Bbb";
    private static final String dialect1 = "Zzz";
    private static final String spokenLanguage1 = "Ccc";
    private static final String spokenLanguage2 = "Ddd";
    private static final List<String> LIST_YES = List.of("Yes");

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

    @Test
    void populateAppellantInterpreterLanguageFieldsIfRequired() {
        when(asylumCase.read(INTERPRETER_LANGUAGE))
            .thenReturn(Optional.of(Arrays.asList(
                new IdValue<>("1", interpreterLanguage1),
                new IdValue<>("2", interpreterLanguage2),
                new IdValue<>("3", interpreterLanguage3)))
            );
        when(interpreterLanguage1.getLanguage()).thenReturn(spokenLanguage1);
        when(interpreterLanguage1.getLanguageDialect()).thenReturn(dialect1);
        when(interpreterLanguage2.getLanguage()).thenReturn(spokenLanguage2);
        when(interpreterLanguage2.getLanguageDialect()).thenReturn(dialect1);
        when(interpreterLanguage3.getLanguage()).thenReturn(signLanguage1);
        when(interpreterLanguage3.getLanguageDialect()).thenReturn(dialect1);

        HandlerUtils.populateAppellantInterpreterLanguageFieldsIfRequired(asylumCase);

        verify(asylumCase, times(1)).write(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.YES);
        verify(asylumCase, times(1)).write(APPELLANT_INTERPRETER_LANGUAGE_CATEGORY,
            List.of(SPOKEN_LANGUAGE_INTERPRETER.getValue(),
                SIGN_LANGUAGE_INTERPRETER.getValue()));

        ArgumentCaptor<InterpreterLanguageRefData> languageCaptor =
            ArgumentCaptor.forClass(InterpreterLanguageRefData.class);
        verify(asylumCase, times(1)).write(eq(APPELLANT_INTERPRETER_SPOKEN_LANGUAGE), languageCaptor.capture());
        verify(asylumCase, times(1)).write(eq(APPELLANT_INTERPRETER_SIGN_LANGUAGE), languageCaptor.capture());

        List<InterpreterLanguageRefData> capturedLanguages = languageCaptor.getAllValues();
        assertNull(capturedLanguages.get(0).getLanguageRefData());
        assertEquals(LIST_YES, capturedLanguages.get(0).getLanguageManualEntry());
        assertEquals("Ccc Zzz; Ddd Zzz", capturedLanguages.get(0).getLanguageManualEntryDescription());
        assertNull(capturedLanguages.get(1).getLanguageRefData());
        assertEquals(LIST_YES, capturedLanguages.get(1).getLanguageManualEntry());
        assertEquals("Sign language - Aaa Zzz", capturedLanguages.get(1).getLanguageManualEntryDescription());
    }

    @Test
    void populateAppellantInterpreterLanguageFieldsIfRequired_interpreterServicesNotNeeded() {
        when(asylumCase.read(IS_INTERPRETER_SERVICES_NEEDED)).thenReturn(Optional.of(YesOrNo.NO));

        HandlerUtils.populateAppellantInterpreterLanguageFieldsIfRequired(asylumCase);

        verify(asylumCase,
            never()).write(IS_INTERPRETER_SERVICES_NEEDED, YesOrNo.YES);
    }
}