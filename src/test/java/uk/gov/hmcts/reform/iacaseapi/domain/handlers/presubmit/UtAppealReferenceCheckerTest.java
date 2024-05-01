package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UT_APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UT_INSTRUCTION_DATE;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UtAppealReferenceCheckerTest {

    @Mock private DateProvider mockDateProvider;
    @Mock private Callback<AsylumCase> mockCallback;
    @Mock private CaseDetails<AsylumCase> mockCaseDetails;
    @Mock private AsylumCase mockAsylumCase;

    private UtAppealReferenceChecker utAppealReferenceChecker;

    @BeforeEach
    void setUp() {
        utAppealReferenceChecker = new UtAppealReferenceChecker(mockDateProvider);
        when(mockCallback.getCaseDetails()).thenReturn(mockCaseDetails);
        when(mockCaseDetails.getCaseData()).thenReturn(mockAsylumCase);
        when(mockCallback.getEvent()).thenReturn(Event.MARK_AS_READY_FOR_UT_TRANSFER);
    }

    @ParameterizedTest
    @ValueSource(strings = {"UI-2005-256985", "UI-2010-256985", "UI-2005-123654"})
    void should_allow_correct_appeal_reference(String appealRef) {
        when(mockAsylumCase.read(UT_APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealRef));
        when(mockCallback.getPageId()).thenReturn("appealReference");
        PreSubmitCallbackResponse<AsylumCase> response = utAppealReferenceChecker.handle(PreSubmitCallbackStage.MID_EVENT, mockCallback);

        assertNotNull(response);
        assertTrue(response.getErrors().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"UI-2005-1236544", "UT-2010-256985", "UI-test-123654"})
    void should_not_allow_incorrect_appeal_reference(String appealRef) {
        when(mockAsylumCase.read(UT_APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.of(appealRef));
        when(mockCallback.getPageId()).thenReturn("appealReference");
        PreSubmitCallbackResponse<AsylumCase> response = utAppealReferenceChecker.handle(PreSubmitCallbackStage.MID_EVENT, mockCallback);

        assertNotNull(response);
        assertTrue(response.getErrors().contains("Enter the Upper Tribunal reference number in the correct format.  The Upper Tribunal reference number is in the format UI-Year of submission-6 digit number, for example UI-2020-123456."));
    }

    @Test
    void should_throw_error_for_missing_appeal_ref() {
        when(mockCallback.getPageId()).thenReturn("appealReference");
        when(mockAsylumCase.read(UT_APPEAL_REFERENCE_NUMBER, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utAppealReferenceChecker.handle(PreSubmitCallbackStage.MID_EVENT, mockCallback))
                .hasMessage("UT Appeal Reference is missing")
                .isInstanceOf(RequiredFieldMissingException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2022-01-20", "2023-05-11", "2020-01-20"})
    void should_allow_correct_instruction_date(String instructionDate) {
        when(mockDateProvider.now()).thenReturn(LocalDate.parse("2023-05-11"));
        when(mockAsylumCase.read(UT_INSTRUCTION_DATE, String.class)).thenReturn(Optional.of(instructionDate));
        when(mockCallback.getPageId()).thenReturn("instructionDate");
        PreSubmitCallbackResponse<AsylumCase> response = utAppealReferenceChecker.handle(PreSubmitCallbackStage.MID_EVENT, mockCallback);

        assertNotNull(response);
        assertTrue(response.getErrors().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-01-20", "2023-11-05", "2023-05-20"})
    void should_not_allow_incorrect_instruction_date(String instructionDate) {
        when(mockDateProvider.now()).thenReturn(LocalDate.parse("2023-05-11"));
        when(mockAsylumCase.read(UT_INSTRUCTION_DATE, String.class)).thenReturn(Optional.of(instructionDate));
        when(mockCallback.getPageId()).thenReturn("instructionDate");
        PreSubmitCallbackResponse<AsylumCase> response = utAppealReferenceChecker.handle(PreSubmitCallbackStage.MID_EVENT, mockCallback);

        assertNotNull(response);
        assertTrue(response.getErrors().contains("The date entered is not valid for When was the First-tier Tribunal instructed to transfer this appeal to the Upper Tribunal"));
    }

    @Test
    void should_throw_error_for_missing_instruction_date() {
        when(mockCallback.getPageId()).thenReturn("instructionDate");
        when(mockAsylumCase.read(UT_INSTRUCTION_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utAppealReferenceChecker.handle(PreSubmitCallbackStage.MID_EVENT, mockCallback))
                .hasMessage("UT Instruction Date is not present")
                .isInstanceOf(RequiredFieldMissingException.class);
    }


    @ParameterizedTest
    @ValueSource(strings = {"appealReference", "instructionDate", "testString"})
    void should_be_invoked_for_correct_event(String pageId) {
        for (Event event: Event.values()) {
            for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
                when(mockCallback.getPageId()).thenReturn(pageId);
                when(mockCallback.getEvent()).thenReturn(event);
                if (event == Event.MARK_AS_READY_FOR_UT_TRANSFER && stage == PreSubmitCallbackStage.MID_EVENT
                    && (pageId.equals("instructionDate") || pageId.equals("appealReference"))) {
                    assertTrue(utAppealReferenceChecker.canHandle(stage, mockCallback));
                } else {
                    assertFalse(utAppealReferenceChecker.canHandle(stage, mockCallback));
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> utAppealReferenceChecker.canHandle(null, mockCallback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> utAppealReferenceChecker.canHandle(PreSubmitCallbackStage.MID_EVENT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> utAppealReferenceChecker.handle(null, mockCallback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> utAppealReferenceChecker.handle(PreSubmitCallbackStage.MID_EVENT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> utAppealReferenceChecker.handle(PreSubmitCallbackStage.ABOUT_TO_START, mockCallback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> utAppealReferenceChecker.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, mockCallback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

}
