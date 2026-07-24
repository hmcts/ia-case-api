package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PinInPostDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CcdDataService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOIN_APPEAL_PIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.JOIN_APPEAL_CONFIRMATION;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class NonLegalRepJoinAppealHandlerTest {

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
    private PinInPostDetails pinInPostDetails;
    @Mock
    private CcdDataService ccdDataService;

    private final NonLegalRepDetails oldNlrDetails = NonLegalRepDetails.builder()
        .emailAddress("someEmail")
        .givenNames("someGivenNames")
        .familyName("someFamilyName")
        .idamId("someIdamId")
        .build();


    private final NonLegalRepDetails oldNlrWithoutIdamIdDetails = NonLegalRepDetails.builder()
        .emailAddress("someEmail")
        .givenNames("someGivenNames")
        .familyName("someFamilyName")
        .build();

    private final NonLegalRepDetails newNlrDetails = NonLegalRepDetails.builder()
        .emailAddress("someChangedEmail")
        .givenNames("someChangedGivenNames")
        .familyName("someChangedFamilyName")
        .idamId("someChangedIdamId")
        .build();

    private NonLegalRepJoinAppealHandler nonLegalRepJoinAppealHandler;

    @BeforeEach
    public void setUp() {
        nonLegalRepJoinAppealHandler = new NonLegalRepJoinAppealHandler(ccdDataService);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);
    }

    @Test
    void should_set_pin_used_yes_if_no_previous_nlr() {
        when(callback.getEvent()).thenReturn(JOIN_APPEAL_CONFIRMATION);
        when(asylumCase.read(JOIN_APPEAL_PIN, PinInPostDetails.class)).thenReturn(Optional.of(pinInPostDetails));
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(newNlrDetails));
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            nonLegalRepJoinAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(pinInPostDetails).setPinUsed(YesOrNo.YES);
        verify(asylumCase).write(JOIN_APPEAL_PIN, pinInPostDetails);
    }

    @Test
    void should_give_access_to_new_nlr() {
        when(callback.getEvent()).thenReturn(JOIN_APPEAL_CONFIRMATION);
        when(asylumCase.read(JOIN_APPEAL_PIN, PinInPostDetails.class)).thenReturn(Optional.of(pinInPostDetails));
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(newNlrDetails));
        when(callback.getCaseDetails().getId()).thenReturn(12345L);
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            nonLegalRepJoinAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(pinInPostDetails).setPinUsed(YesOrNo.YES);
        verify(asylumCase).write(JOIN_APPEAL_PIN, pinInPostDetails);
        verify(ccdDataService).giveUserAccessToCase(12345L, "someChangedIdamId");
    }

    @Test
    void should_throw_if_no_nlr_details() {
        when(callback.getEvent()).thenReturn(JOIN_APPEAL_CONFIRMATION);
        when(asylumCase.read(JOIN_APPEAL_PIN, PinInPostDetails.class)).thenReturn(Optional.of(pinInPostDetails));
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.empty());
        when(callback.getCaseDetails().getId()).thenReturn(12345L);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> nonLegalRepJoinAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));

        assertEquals("Non legal rep details are not present", exception.getMessage());
    }

    @Test
    void should_throw_if_create_assignment_fails() {
        when(callback.getEvent()).thenReturn(JOIN_APPEAL_CONFIRMATION);
        when(asylumCase.read(JOIN_APPEAL_PIN, PinInPostDetails.class)).thenReturn(Optional.of(pinInPostDetails));
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(newNlrDetails));
        when(callback.getCaseDetails().getId()).thenReturn(12345L);
        RuntimeException someError = new RuntimeException("some error");
        doThrow(someError)
            .when(ccdDataService).giveUserAccessToCase(anyLong(), anyString());
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> nonLegalRepJoinAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));

        assertEquals("Failed to assign case role to the new non legal rep: " + someError.getMessage(), exception.getMessage());
    }

    @Test
    void should_revoke_previous_nlr_access() {
        when(callback.getEvent()).thenReturn(JOIN_APPEAL_CONFIRMATION);
        when(asylumCase.read(JOIN_APPEAL_PIN, PinInPostDetails.class)).thenReturn(Optional.of(pinInPostDetails));
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(newNlrDetails));
        when(callback.getCaseDetails().getId()).thenReturn(12345L);
        when(asylumCaseBefore.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(oldNlrDetails));
        List<Assignment> assignments = List.of(
            Assignment.builder().id("assignmentId1").build(),
            Assignment.builder().id("assignmentId2").build()
        );
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            nonLegalRepJoinAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(pinInPostDetails).setPinUsed(YesOrNo.YES);
        verify(asylumCase).write(JOIN_APPEAL_PIN, pinInPostDetails);
        verify(ccdDataService).revokeUserAccessToCase(12345L, "someIdamId");
    }

    @Test
    void should_not_revoke_if_previous_nlr_details_has_no_idam_id() {
        when(callback.getEvent()).thenReturn(JOIN_APPEAL_CONFIRMATION);
        when(asylumCase.read(JOIN_APPEAL_PIN, PinInPostDetails.class)).thenReturn(Optional.of(pinInPostDetails));
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(newNlrDetails));
        when(callback.getCaseDetails().getId()).thenReturn(12345L);
        when(asylumCaseBefore.read(NLR_DETAILS, NonLegalRepDetails.class)).thenReturn(Optional.of(oldNlrWithoutIdamIdDetails));
        List<Assignment> assignments = List.of(
            Assignment.builder().id("assignmentId1").build(),
            Assignment.builder().id("assignmentId2").build()
        );
        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            nonLegalRepJoinAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        verify(pinInPostDetails).setPinUsed(YesOrNo.YES);
        verify(asylumCase).write(JOIN_APPEAL_PIN, pinInPostDetails);
        verify(ccdDataService, never()).revokeUserAccessToCase(anyLong(), anyString());
    }


    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> nonLegalRepJoinAppealHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {
        when(callback.getEvent()).thenReturn(JOIN_APPEAL_CONFIRMATION);
        assertTrue(nonLegalRepJoinAppealHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, mode = EnumSource.Mode.EXCLUDE, names = {"JOIN_APPEAL_CONFIRMATION"})
    void it_cannot_handle_incorrect_callback_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(nonLegalRepJoinAppealHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, mode = EnumSource.Mode.EXCLUDE, names = {"ABOUT_TO_SUBMIT"})
    void it_cannot_handle_incorrect_callback_stage(PreSubmitCallbackStage callbackStage) {
        when(callback.getEvent()).thenReturn(JOIN_APPEAL_CONFIRMATION);
        assertFalse(nonLegalRepJoinAppealHandler.canHandle(callbackStage, callback));
    }

    @Test
    void should_not_allow_null_arguments() {

        NullPointerException nullCallbackStage = assertThrows(NullPointerException.class,
            () -> nonLegalRepJoinAppealHandler.canHandle(null, callback));
        NullPointerException nullCallback = assertThrows(NullPointerException.class,
            () -> nonLegalRepJoinAppealHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null));

        assertEquals("callbackStage must not be null", nullCallbackStage.getMessage());
        assertEquals("callback must not be null", nullCallback.getMessage());
    }

    @Test
    void dispatch_priority_should_be_early() {
        assertEquals(DispatchPriority.EARLY, nonLegalRepJoinAppealHandler.getDispatchPriority());
    }
}
