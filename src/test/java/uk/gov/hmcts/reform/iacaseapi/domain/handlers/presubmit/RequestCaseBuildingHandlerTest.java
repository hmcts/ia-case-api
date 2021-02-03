package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REASON_TO_FORCE_REQUEST_CASE_BUILDING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.lang.Nullable;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class RequestCaseBuildingHandlerTest {

    @Mock
    private static Callback<AsylumCase> callback;
    @Mock
    private static CaseDetails<AsylumCase> caseDetails;
    @Mock
    private UserDetails userDetails;
    @Mock
    private static AsylumCase asylumCase;
    @Mock
    private Appender<CaseNote> appender;
    @Mock
    private DateProvider dateProvider;

    @InjectMocks
    private RequestCaseBuildingHandler requestCaseBuildingHandler;

    private static Object[] generateAsylumCaseWithDifferentCaseNotesScenarios() {
        String reasonToForceCase = "some reason";
        String otherReasonToForceCase = "some other reason";
        AsylumCase customAsylumCase = buildAsylumCaseWithNoCaseNotesAndReason(reasonToForceCase);
        AsylumCase customAsylumCaseWithSomeOtherReason =
            buildAsylumCaseWithNoCaseNotesAndReason(otherReasonToForceCase);

        AsylumCase customAsylumCaseWithExistingCaseNotes = buildAsylumCaseWithNoCaseNotesAndReason(reasonToForceCase);
        IdValue<CaseNote> idCaseNote1 = buildIdCaseNote(reasonToForceCase, "1");
        IdValue<CaseNote> idCaseNote2 = buildIdCaseNote(otherReasonToForceCase, "2");
        List<IdValue<CaseNote>> caseNotes = Arrays.asList(idCaseNote1, idCaseNote2);
        customAsylumCaseWithExistingCaseNotes.write(CASE_NOTES, caseNotes);

        return new Object[] {
            new Object[] {
                new AsylumCase(),
                null,
                null
            },
            new Object[] {
                customAsylumCase,
                buildIdCaseNote(reasonToForceCase, "1"),
                Collections.singletonList(buildIdCaseNote(reasonToForceCase, "1"))
            },
            new Object[] {
                customAsylumCaseWithSomeOtherReason,
                buildIdCaseNote(otherReasonToForceCase, "1"),
                Collections.singletonList(buildIdCaseNote(otherReasonToForceCase, "1"))
            },
            new Object[] {
                customAsylumCaseWithExistingCaseNotes,
                buildIdCaseNote(reasonToForceCase, "3"),
                Arrays.asList(idCaseNote1, idCaseNote2, buildIdCaseNote(reasonToForceCase, "3"))
            }
        };
    }

    private static CaseNote buildCaseNote(String reason) {
        return new CaseNote("Force case from Awaiting Respondent Evidence to Case Building",
            reason, "some forename some surname", LocalDate.now().toString());
    }

    private static AsylumCase buildAsylumCaseWithNoCaseNotesAndReason(String reasonToForceCase) {
        AsylumCase customAsylumCase = new AsylumCase();
        customAsylumCase.write(REASON_TO_FORCE_REQUEST_CASE_BUILDING, reasonToForceCase);
        return customAsylumCase;
    }

    private static IdValue<CaseNote> buildIdCaseNote(String noteDescription, String id) {
        return new IdValue<>(id, buildCaseNote(noteDescription));
    }

    private static Object[] generateExceptionScenarios() {
        CaseDetails<AsylumCase> dummyCaseDetails = new CaseDetails<>(1L, "", State.APPEAL_STARTED, "Asylum",
            asylumCase, LocalDateTime.now(), "PUBLIC");
        Callback<AsylumCase> dummyCallback = new Callback<>(dummyCaseDetails, Optional.empty(), Event.SEND_DIRECTION);

        return new Object[] {
            new Object[] {ABOUT_TO_START, dummyCallback, "Cannot handle callback"},
            new Object[] {ABOUT_TO_SUBMIT, dummyCallback, "Cannot handle callback"}
        };
    }

    private static Object[] generateNullArgsScenarios() {
        return new Object[] {
            new Object[] {null, callback, "callbackStage must not be null"},
            new Object[] {ABOUT_TO_SUBMIT, null, "callback must not be null"}
        };
    }

    @Test
    void should_set_the_flag_on_valid_case_data() {
        mockCallback(asylumCase);

        requestCaseBuildingHandler.handle(ABOUT_TO_SUBMIT, callback);

        verify(asylumCase).write(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE, YesOrNo.NO);
    }

    private void mockCallback(AsylumCase asylumCase) {
        when(callback.getEvent()).thenReturn(Event.REQUEST_CASE_BUILDING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @ParameterizedTest
    @MethodSource("generateAsylumCaseWithDifferentCaseNotesScenarios")
    void givenValidCallback_shouldCopyReasonToCaseNote(AsylumCase customAsylumCase,
                                                              IdValue<CaseNote> expectedIdCaseNote,
                                                              List<IdValue<CaseNote>> expectedAppendedCaseNotes) {
        mockCallback(customAsylumCase);
        mockUserDetailsProvider();

        given(dateProvider.now()).willReturn(LocalDate.now());

        given(appender.append(any(CaseNote.class), anyList())).willReturn(expectedAppendedCaseNotes);
        given(callback.getEvent()).willReturn(Event.FORCE_REQUEST_CASE_BUILDING);

        PreSubmitCallbackResponse<AsylumCase> currentResult =
            requestCaseBuildingHandler.handle(ABOUT_TO_SUBMIT, callback);

        Optional<List<IdValue<CaseNote>>> currentCaseNotesOptional = currentResult.getData().read(CASE_NOTES);

        if (currentCaseNotesOptional.isPresent()) {
            List<IdValue<CaseNote>> caseNotes = currentCaseNotesOptional.get();
            assertThat(caseNotes).isNotEmpty();
            assertThat(caseNotes)
                .filteredOn(idValue -> idValue.getId().equals(expectedIdCaseNote.getId()))
                .containsOnlyOnce(expectedIdCaseNote);
        } else if (expectedIdCaseNote == null) {
            assertTrue(true, "No expected case note here");
        } else {
            fail("no case note present");
        }
        Optional<String> reason = currentResult.getData().read(REASON_TO_FORCE_REQUEST_CASE_BUILDING, String.class);
        assertThat(reason.isPresent()).isFalse();
    }

    private void mockUserDetailsProvider() {
        when(userDetails.getForenameAndSurname()).thenReturn("some forename" + "some surname");
    }

    @ParameterizedTest
    @MethodSource("generateExceptionScenarios")
    void handling_should_throw_if_cannot_actually_handle(PreSubmitCallbackStage callbackStage,
                                                                Callback<AsylumCase> callback,
                                                                String expectedMsg) {

        assertThatThrownBy(() -> requestCaseBuildingHandler.handle(callbackStage, callback))
            .hasMessage(expectedMsg)
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(asylumCase, never()).write(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE, YesOrNo.NO);
    }

    @ParameterizedTest
    @CsvSource({
        "ABOUT_TO_SUBMIT, REQUEST_CASE_BUILDING, true",
        "ABOUT_TO_SUBMIT, FORCE_REQUEST_CASE_BUILDING, true",
        "MID_EVENT, REQUEST_CASE_BUILDING, false",
        "ABOUT_TO_SUBMIT, UPLOAD_HEARING_RECORDING, false"
    })
    void canHandle(PreSubmitCallbackStage callbackStage, Event event, boolean expectedResult) {
        if (!callbackStage.equals(PreSubmitCallbackStage.MID_EVENT)) {
            given(callback.getEvent()).willReturn(event);
        }

        boolean actualResult = requestCaseBuildingHandler.canHandle(callbackStage, callback);

        assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @MethodSource("generateNullArgsScenarios")
    void given_null_callback_should_throw_exception(@Nullable PreSubmitCallbackStage callbackStage,
                                                           @Nullable Callback<AsylumCase> callback,
                                                           String expectedMessage) {

        assertThatThrownBy(() -> requestCaseBuildingHandler.canHandle(callbackStage, callback))
            .hasMessage(expectedMessage)
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
