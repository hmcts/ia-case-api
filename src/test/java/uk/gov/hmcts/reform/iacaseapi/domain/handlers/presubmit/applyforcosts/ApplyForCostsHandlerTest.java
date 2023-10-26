package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.APPLY_FOR_COSTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.END_APPEAL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.ApplyForCostsAppender;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ApplyForCostsHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    @Mock
    private ApplyForCostsAppender applyForCostsAppender;

    @Mock
    private UserDetails userDetails;
    @Mock
    private UserDetailsHelper userDetailsHelper;
    @InjectMocks
    private ApplyForCostsHandler applyForCostsHandler;
    private List<IdValue<Document>> argumentsAndEvidenceDocuments =
            List.of(new IdValue<>("1",
                    new Document("http://localhost/documents/123456",
                            "http://localhost/documents/123456",
                            "DocumentName.pdf")));

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        applyForCostsHandler = new ApplyForCostsHandler(applyForCostsAppender);
        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.of("test"));
    }

    @Test
    void should_append_apply_for_costs() {
        final List<IdValue<ApplyForCosts>> existingAppliesForCosts = new ArrayList<>();
        final List<IdValue<ApplyForCosts>> newAppliesForCosts = new ArrayList<>();
        when(callback.getEvent()).thenReturn(APPLY_FOR_COSTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        DynamicList appliedCostsTypes = new DynamicList("UNREASONABLE_COSTS");

        when(asylumCase.read(APPLIED_COSTS_TYPES, DynamicList.class)).thenReturn(Optional.of(appliedCostsTypes));
        when(asylumCase.read(ARGUMENTS_AND_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(argumentsAndEvidenceDocuments));
        when(asylumCase.read(APPLY_FOR_COSTS_HEARING_TYPE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPLY_FOR_COSTS_HEARING_TYPE_EXPLANATION, String.class)).thenReturn(Optional.of("test"));
        when(asylumCase.read(APPLY_FOR_COSTS_DECISION, String.class)).thenReturn(Optional.of("test"));
        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.of("test"));

        when(applyForCostsAppender.append(
                existingAppliesForCosts,
                TypesOfAppliedCosts.UNREASONABLE_COSTS.toString(),
                "test",
                argumentsAndEvidenceDocuments,
                Collections.emptyList(),
                YesOrNo.YES,
                "test",
                "test",
                "test")).thenReturn(newAppliesForCosts);

        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.LEGAL_REPRESENTATIVE);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                applyForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1))
                .read(APPLIED_COSTS_TYPES, DynamicList.class);
        verify(asylumCase, times(1))
                .read(ARGUMENTS_AND_EVIDENCE_DETAILS, String.class);
        verify(asylumCase, times(1))
                .read(ARGUMENTS_AND_EVIDENCE_DOCUMENTS);
        verify(asylumCase, times(1))
                .read(SCHEDULE_OF_COSTS_DOCUMENTS);
        verify(asylumCase, times(1))
                .read(APPLY_FOR_COSTS_HEARING_TYPE, YesOrNo.class);
        verify(asylumCase, times(1))
                .read(APPLY_FOR_COSTS_HEARING_TYPE_EXPLANATION, String.class);
        verify(asylumCase, times(1))
                .read(LEGAL_REP_NAME, String.class);
        verify(asylumCase, times(1))
                .read(APPLIES_FOR_COSTS);
        verify(asylumCase, times(1))
                .write(APPLIES_FOR_COSTS, newAppliesForCosts);
        verify(asylumCase, times(1))
                .write(IS_APPLIED_FOR_COSTS, YesOrNo.YES);
        verify(asylumCase, times(1))
                .clear(APPLIED_COSTS_TYPES);
        verify(asylumCase, times(1))
                .clear(ARGUMENTS_AND_EVIDENCE_DETAILS);
        verify(asylumCase, times(1))
                .clear(ARGUMENTS_AND_EVIDENCE_DOCUMENTS);
        verify(asylumCase, times(1))
                .clear(SCHEDULE_OF_COSTS_DOCUMENTS);
        verify(asylumCase, times(1))
                .clear(APPLY_FOR_COSTS_HEARING_TYPE);
        verify(asylumCase, times(1))
                .clear(APPLY_FOR_COSTS_HEARING_TYPE_EXPLANATION);
    }

    @Test
    void should_throw_on_missing_types_of_applied_costs_reason() {
        when(callback.getEvent()).thenReturn(APPLY_FOR_COSTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        Assertions
                .assertThatThrownBy(() -> applyForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("typesOfAppliedCosts is not present");
    }

    @Test
    void should_throw_on_missing_arguments_and_evidence_documents_reason() {
        when(callback.getEvent()).thenReturn(APPLY_FOR_COSTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(APPLIED_COSTS_TYPES, DynamicList.class)).thenReturn(Optional.of(new DynamicList("UNREASONABLE_COSTS")));
        when(asylumCase.read(APPLY_FOR_COSTS_HEARING_TYPE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPLY_FOR_COSTS_HEARING_TYPE_EXPLANATION, String.class)).thenReturn(Optional.of("test"));
        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.of("test"));

        Assertions
                .assertThatThrownBy(() -> applyForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("argumentsAndEvidenceDocuments are not present");
    }

    @Test
    void should_throw_on_missing_apply_for_costs_hearing_type_reason() {
        when(callback.getEvent()).thenReturn(APPLY_FOR_COSTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLIED_COSTS_TYPES, DynamicList.class)).thenReturn(Optional.of(new DynamicList("UNREASONABLE_COSTS")));
        when(asylumCase.read(ARGUMENTS_AND_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(argumentsAndEvidenceDocuments));

        Assertions
                .assertThatThrownBy(() -> applyForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("applyForCostsHearingType is not present");
    }

    @Test
    void should_throw_on_missing_apply_for_costs_hearing_type_explanation_reason() {
        when(callback.getEvent()).thenReturn(APPLY_FOR_COSTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLIED_COSTS_TYPES, DynamicList.class)).thenReturn(Optional.of(new DynamicList("UNREASONABLE_COSTS")));
        when(asylumCase.read(ARGUMENTS_AND_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(argumentsAndEvidenceDocuments));
        when(asylumCase.read(APPLY_FOR_COSTS_HEARING_TYPE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        Assertions
                .assertThatThrownBy(() -> applyForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("applyForCostsHearingTypeExplanation is not present");
    }

    @Test
    void should_throw_on_legal_rep_reason() {
        when(callback.getEvent()).thenReturn(APPLY_FOR_COSTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(APPLIED_COSTS_TYPES, DynamicList.class)).thenReturn(Optional.of(new DynamicList("UNREASONABLE_COSTS")));
        when(asylumCase.read(ARGUMENTS_AND_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(argumentsAndEvidenceDocuments));
        when(asylumCase.read(APPLY_FOR_COSTS_HEARING_TYPE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPLY_FOR_COSTS_HEARING_TYPE_EXPLANATION, String.class)).thenReturn(Optional.of("test"));

        Assertions
                .assertThatThrownBy(() -> applyForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("legalRepName is not present");
    }

    @Test
    void handling_should_throw_if_stage_is_incorrect_handle() {
        assertThatThrownBy(() -> applyForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_event_is_incorrect_handle() {
        when(callback.getEvent()).thenReturn(END_APPEAL);
        assertThatThrownBy(() -> applyForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_it_is_internal_case_handle() {
        when(callback.getEvent()).thenReturn(APPLY_FOR_COSTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(LEGAL_REP_NAME, String.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> applyForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> applyForCostsHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> applyForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}