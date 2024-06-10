package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.CONSIDER_MAKING_COSTS_ORDER;

import java.util.ArrayList;
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
class ConsiderMakingCostsOrderHandlerTest {
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
    private ConsiderMakingCostsOrderHandler considerMakingCostsOrderHandler;
    private String testGenericValue = "test";
    private String unreasonableCosts = "UNREASONABLE_COSTS";
    private List<IdValue<Document>> responseEvidenceDocuments = List.of(new IdValue<>("1", new Document("http://localhost/documents/123456", "http://localhost/documents/123456", "DocumentName.pdf")));

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        considerMakingCostsOrderHandler = new ConsiderMakingCostsOrderHandler(applyForCostsAppender);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(CONSIDER_MAKING_COSTS_ORDER);
    }

    @Test
    void should_append_apply_for_costs() {
        ApplyForCosts applyForCosts = new ApplyForCosts(
            "Wasted Costs",
            "Evidence Details",
            responseEvidenceDocuments,
            responseEvidenceDocuments,
            YesOrNo.YES,
            "Hearing explanation",
            "Pending",
            "Home Office",
            "2023-11-10",
            "Legal Rep Name",
            "OOT explanation",
            responseEvidenceDocuments,
            YesOrNo.NO,
            "Legal Representative"
        );

        List<IdValue<ApplyForCosts>> existingAppliesForCosts = new ArrayList<>();
        existingAppliesForCosts.add(new IdValue<>("1", applyForCosts));

        when(callback.getEvent()).thenReturn(CONSIDER_MAKING_COSTS_ORDER);
        DynamicList appliedCostsTypes = new DynamicList(unreasonableCosts);

        when(asylumCase.read(JUDGE_APPLIED_COSTS_TYPES, DynamicList.class)).thenReturn(Optional.of(appliedCostsTypes));
        when(asylumCase.read(RESPONDENT_TO_COSTS_ORDER, String.class)).thenReturn(Optional.of("Legal representative"));
        when(asylumCase.read(TRIBUNAL_CONSIDERING_REASON, String.class)).thenReturn(Optional.of(testGenericValue));
        when(asylumCase.read(JUDGE_EVIDENCE_FOR_COSTS_ORDER)).thenReturn(Optional.of(responseEvidenceDocuments));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(existingAppliesForCosts));

        when(applyForCostsAppender.append(existingAppliesForCosts, CostsDecision.PENDING.toString(), unreasonableCosts, testGenericValue, responseEvidenceDocuments, "Legal representative")).thenReturn(existingAppliesForCosts);
        when(userDetailsHelper.getLoggedInUserRoleLabel(userDetails)).thenReturn(UserRoleLabel.JUDGE);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse = considerMakingCostsOrderHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).read(JUDGE_APPLIED_COSTS_TYPES, DynamicList.class);
        verify(asylumCase, times(1)).read(RESPONDENT_TO_COSTS_ORDER, String.class);
        verify(asylumCase, times(1)).read(TRIBUNAL_CONSIDERING_REASON, String.class);
        verify(asylumCase, times(1)).read(JUDGE_EVIDENCE_FOR_COSTS_ORDER);
        verify(asylumCase, times(1)).read(APPLIES_FOR_COSTS);

        verify(asylumCase, times(1)).write(APPLIES_FOR_COSTS, existingAppliesForCosts);
        verify(asylumCase, times(1)).write(IS_APPLIED_FOR_COSTS, YesOrNo.YES);

        verify(asylumCase, times(1)).clear(JUDGE_APPLIED_COSTS_TYPES);
        verify(asylumCase, times(1)).clear(RESPONDENT_TO_COSTS_ORDER);
        verify(asylumCase, times(1)).clear(TRIBUNAL_CONSIDERING_REASON);
        verify(asylumCase, times(1)).clear(JUDGE_EVIDENCE_FOR_COSTS_ORDER);
    }

    @Test
    void should_throw_on_missing_types_of_judge_applied_costs_type_reason() {
        when(callback.getEvent()).thenReturn(CONSIDER_MAKING_COSTS_ORDER);

        Assertions.assertThatThrownBy(() -> considerMakingCostsOrderHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)).isExactlyInstanceOf(IllegalStateException.class).hasMessage("judgeAppliedCostsTypes is not present");
    }

    @Test
    void should_throw_on_missing_respondent_to_costs_order_reason() {
        when(asylumCase.read(JUDGE_APPLIED_COSTS_TYPES, DynamicList.class)).thenReturn(Optional.of(new DynamicList(unreasonableCosts)));

        Assertions.assertThatThrownBy(() -> considerMakingCostsOrderHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)).isExactlyInstanceOf(IllegalStateException.class).hasMessage("respondentToCostsOrder is not present");
    }

    @Test
    void should_throw_on_missing_tribunal_considering_reason() {
        when(asylumCase.read(JUDGE_APPLIED_COSTS_TYPES, DynamicList.class)).thenReturn(Optional.of(new DynamicList(unreasonableCosts)));
        when(asylumCase.read(RESPONDENT_TO_COSTS_ORDER, String.class)).thenReturn(Optional.of("Legal representative"));

        Assertions.assertThatThrownBy(() -> considerMakingCostsOrderHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback)).isExactlyInstanceOf(IllegalStateException.class).hasMessage("tribunalConsideringReason is not present");
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> considerMakingCostsOrderHandler.canHandle(null, callback)).hasMessage("callbackStage must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> considerMakingCostsOrderHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null)).hasMessage("callback must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> considerMakingCostsOrderHandler.handle(null, callback)).hasMessage("callbackStage must not be null").isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> considerMakingCostsOrderHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null)).hasMessage("callback must not be null").isExactlyInstanceOf(NullPointerException.class);
    }
}