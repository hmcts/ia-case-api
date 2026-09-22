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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.roleassignment.Assignment;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CcdDataService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_NON_LEGAL_REP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_NON_LEGAL_REP_JOINED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_SPONSOR_SAME_AS_NLR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOIN_APPEAL_PIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RevokeCitizenAccessHandlerTest {

    @Mock
    private CcdDataService ccdDataService;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private RevokeCitizenAccessHandler handler;

    private final long caseId = 12345L;
    private final String idamId = "user-123";
    private final String roleAssignmentId = "role-456";

    @BeforeEach
    void setUp() {
        handler = new RevokeCitizenAccessHandler(ccdDataService);
        when(callback.getEvent()).thenReturn(Event.REVOKE_CITIZEN_ACCESS);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(caseDetails.getId()).thenReturn(caseId);
        when(callback.getCaseDetails()).thenReturn(caseDetails);

    }

    @Test
    void should_revoke_access() {
        Value value = new Value(idamId, "User Name");
        DynamicList dynamicList = new DynamicList(value, List.of(value));
        when(asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_DL, DynamicList.class))
            .thenReturn(Optional.of(dynamicList));

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertTrue(response.getErrors().isEmpty());

        verify(ccdDataService).revokeUserAccessToCase(caseId, idamId);
    }

    @Test
    void should_clear_nlr_details_if_idam_id_equal() {
        Value value = new Value(idamId, "User Name");
        DynamicList dynamicList = new DynamicList(value, List.of(value));
        when(asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_DL, DynamicList.class))
            .thenReturn(Optional.of(dynamicList));
        NonLegalRepDetails nlrDetails = NonLegalRepDetails.builder()
            .idamId(idamId)
            .emailAddress("someEmail")
            .givenNames("someGivenNames")
            .familyName("someFamilyName")
            .build();
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class))
            .thenReturn(Optional.of(nlrDetails));


        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertTrue(response.getErrors().isEmpty());

        verify(ccdDataService).revokeUserAccessToCase(caseId, idamId);
        verify(asylumCase).clear(NLR_DETAILS);
        verify(asylumCase).clear(JOIN_APPEAL_PIN);
        verify(asylumCase).clear(IS_SPONSOR_SAME_AS_NLR);
        verify(asylumCase).clear(HAS_NON_LEGAL_REP_JOINED);
        verify(asylumCase).write(HAS_NON_LEGAL_REP, NO);
    }

    @Test
    void should_not_clear_nlr_details_if_idam_id_not_equal() {
        Value value = new Value(idamId, "User Name");
        DynamicList dynamicList = new DynamicList(value, List.of(value));
        when(asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_DL, DynamicList.class))
            .thenReturn(Optional.of(dynamicList));
        NonLegalRepDetails nlrDetails = NonLegalRepDetails.builder()
            .idamId("someOtherIdamId")
            .emailAddress("someEmail")
            .givenNames("someGivenNames")
            .familyName("someFamilyName")
            .build();
        when(asylumCase.read(NLR_DETAILS, NonLegalRepDetails.class))
            .thenReturn(Optional.of(nlrDetails));
        Assignment assignment = Assignment.builder().id(roleAssignmentId).build();

        PreSubmitCallbackResponse<AsylumCase> response = handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertEquals(asylumCase, response.getData());
        assertTrue(response.getErrors().isEmpty());

        verify(ccdDataService).revokeUserAccessToCase(caseId, idamId);
        verify(asylumCase, never()).clear(NLR_DETAILS);
        verify(asylumCase, never()).clear(JOIN_APPEAL_PIN);
        verify(asylumCase, never()).clear(IS_SPONSOR_SAME_AS_NLR);
        verify(asylumCase, never()).clear(HAS_NON_LEGAL_REP_JOINED);
        verify(asylumCase, never()).write(HAS_NON_LEGAL_REP, NO);
    }

    @Test
    void should_throw_when_dynamic_list_is_missing() {
        when(asylumCase.read(AsylumCaseFieldDefinition.REVOKE_ACCESS_DL, DynamicList.class))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Dynamic list of users to revoke access from is not present.");

        verifyNoInteractions(ccdDataService);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"REVOKE_CITIZEN_ACCESS"}, mode = EnumSource.Mode.EXCLUDE)
    void cannot_handle_for_wrong_event(Event event) {
        when(callback.getEvent()).thenReturn(event);
        assertFalse(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @ParameterizedTest
    @EnumSource(value = PreSubmitCallbackStage.class, names = {"ABOUT_TO_SUBMIT"}, mode = EnumSource.Mode.EXCLUDE)
    void cannot_handle_for_wrong_stage(PreSubmitCallbackStage stage) {
        when(callback.getEvent()).thenReturn(Event.REVOKE_CITIZEN_ACCESS);
        assertFalse(handler.canHandle(stage, callback));
    }

    @Test
    void can_handle_only_for_correct_event_and_stage() {
        when(callback.getEvent()).thenReturn(Event.REVOKE_CITIZEN_ACCESS);
        assertTrue(handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback));
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> handler.canHandle(null, callback))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("callbackStage must not be null");

        assertThatThrownBy(() -> handler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("callback must not be null");
    }

    @Test
    void should_throw_if_cannot_handle_callback() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");

        when(callback.getEvent()).thenReturn(Event.REVOKE_CITIZEN_ACCESS);
        assertThatThrownBy(() -> handler.handle(PreSubmitCallbackStage.MID_EVENT, callback))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Cannot handle callback");
    }
}