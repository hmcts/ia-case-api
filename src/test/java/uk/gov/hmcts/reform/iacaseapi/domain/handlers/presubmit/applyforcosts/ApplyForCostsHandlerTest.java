package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.APPLY_FOR_COSTS;

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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TypesOfAppliedCosts;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
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
        when(asylumCase.read(TYPES_OF_APPLIED_COSTS, TypesOfAppliedCosts.class)).thenReturn(Optional.of(TypesOfAppliedCosts.UNREASONABLE_COSTS));
        when(asylumCase.read(APPLY_FOR_COSTS_HEARING_TYPE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(APPLY_FOR_COSTS_HEARING_TYPE_EXPLANATION, String.class)).thenReturn(Optional.of("test"));

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
        when(asylumCase.read(TYPES_OF_APPLIED_COSTS, TypesOfAppliedCosts.class)).thenReturn(Optional.of(TypesOfAppliedCosts.UNREASONABLE_COSTS));
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
        when(asylumCase.read(TYPES_OF_APPLIED_COSTS, TypesOfAppliedCosts.class)).thenReturn(Optional.of(TypesOfAppliedCosts.UNREASONABLE_COSTS));
        when(asylumCase.read(ARGUMENTS_AND_EVIDENCE_DOCUMENTS)).thenReturn(Optional.of(argumentsAndEvidenceDocuments));
        when(asylumCase.read(APPLY_FOR_COSTS_HEARING_TYPE, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        Assertions
                .assertThatThrownBy(() -> applyForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("applyForCostsHearingTypeExplanation is not present");
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> applyForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
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

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = applyForCostsHandler.canHandle(callbackStage, callback);

                if ((event == Event.APPLY_FOR_COSTS)
                        && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }
}