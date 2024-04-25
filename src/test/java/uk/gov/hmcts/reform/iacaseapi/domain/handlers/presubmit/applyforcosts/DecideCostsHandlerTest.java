package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.DECIDE_COSTS_APPLICATION;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.ApplyForCostsProvider;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class DecideCostsHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private ApplyForCostsProvider applyForCostsProvider;
    private static String homeOffice = "Home office";
    private static String legalRep = "Legal representative";
    private ApplyForCosts applyForCosts;
    private String oralHearingdate = "2023-11-10";
    @InjectMocks
    private DecideCostsHandler decideCostsHandler;
    private static List<IdValue<Document>> evidenceDocuments =
        List.of(new IdValue<>("1",
            new Document("http://localhost/documents/123456",
                "http://localhost/documents/123456",
                "DocumentName.pdf")));

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        decideCostsHandler = new DecideCostsHandler(applyForCostsProvider);

        when(callback.getEvent()).thenReturn(DECIDE_COSTS_APPLICATION);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        applyForCosts = new ApplyForCosts(
            "Wasted Costs",
            "Evidence Details",
            evidenceDocuments,
            evidenceDocuments,
            YesOrNo.YES,
            "Hearing explanation",
            "Pending",
            homeOffice,
            "2023-11-10",
            "Legal Rep Name",
            "OOT explanation",
            evidenceDocuments,
            YesOrNo.NO,
            "Legal Representative");

        List<IdValue<ApplyForCosts>> existingAppliesForCosts = new ArrayList<>();
        existingAppliesForCosts.add(new IdValue<>("1", applyForCosts));

        List<Value> applyForCostsDynamicList = new ArrayList<>();
        applyForCostsDynamicList.add(new Value("1", "Costs 1, Wasted costs, 10 Nov 2023"));
        DynamicList dynamicList = new DynamicList(new Value("1", "Costs 1, Wasted costs, 10 Nov 2023"), applyForCostsDynamicList);

        when(asylumCase.read(DECIDE_COSTS_APPLICATION_LIST, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(APPLY_FOR_COSTS_DECISION, CostsDecision.class)).thenReturn(Optional.of(CostsDecision.ORDER_MADE));
        when(asylumCase.read(UPLOAD_COSTS_ORDER)).thenReturn(Optional.of(evidenceDocuments));
        when(asylumCase.read(COSTS_ORAL_HEARING_DATE, String.class)).thenReturn(Optional.of(oralHearingdate));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(existingAppliesForCosts));
        LocalDate dateNow = LocalDate.now();
        when(applyForCostsProvider.formatDate(LocalDate.now())).thenReturn(dateNow.format(DateTimeFormatter.ofPattern("d MMM yyyy")));
        when(applyForCostsProvider.formatDate(oralHearingdate)).thenReturn("10 Nov 2023");
    }

    @ParameterizedTest
    @EnumSource(value = CostsDecisionType.class)
    void should_set_proper_values_for_apply_for_costs_decision(CostsDecisionType costsDecisionType) {
        when(asylumCase.read(COSTS_DECISION_TYPE, CostsDecisionType.class)).thenReturn(Optional.of(costsDecisionType));

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            decideCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());
        assertThat(applyForCosts.getApplyForCostsDecision().equals(CostsDecision.ORDER_MADE.toString()));
        assertThat(applyForCosts.getCostsDecisionType().equals(costsDecisionType.toString()));
        if (costsDecisionType == CostsDecisionType.WITH_AN_ORAL_HEARING) {
            assertThat(applyForCosts.getCostsOralHearingDate().equals(applyForCostsProvider.formatDate(oralHearingdate)));
        }
        assertThat(applyForCosts.getUploadCostsOrder().equals(evidenceDocuments));
        assertThat(applyForCosts.getDateOfDecision().equals(applyForCostsProvider.formatDate(LocalDate.now())));

        List<IdValue<ApplyForCosts>> existingAppliesForCosts = new ArrayList<>();
        existingAppliesForCosts.add(new IdValue<>("1", applyForCosts));

        verify(asylumCase, times(1)).read(DECIDE_COSTS_APPLICATION_LIST, DynamicList.class);
        verify(asylumCase, times(1)).read(APPLY_FOR_COSTS_DECISION, CostsDecision.class);
        verify(asylumCase, times(1)).read(COSTS_DECISION_TYPE, CostsDecisionType.class);
        verify(asylumCase, times(1)).read(UPLOAD_COSTS_ORDER);
        verify(asylumCase, times(1)).read(APPLIES_FOR_COSTS);
        verify(asylumCase, times(1)).write(APPLIES_FOR_COSTS, Optional.of(existingAppliesForCosts));
        verify(asylumCase, times(1)).clear(APPLY_FOR_COSTS_DECISION);
        verify(asylumCase, times(1)).clear(COSTS_DECISION_TYPE);
        verify(asylumCase, times(1)).clear(COSTS_ORAL_HEARING_DATE);
        verify(asylumCase, times(1)).clear(UPLOAD_COSTS_ORDER);
    }

    @Test
    void should_throw_exception_if_decide_costs_list_missing() {
        when(asylumCase.read(DECIDE_COSTS_APPLICATION_LIST, DynamicList.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> decideCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("decideCostsApplicationList is not present");
    }

    @Test
    void should_throw_exception_if_apply_for_costs_decision_missing() {
        when(asylumCase.read(APPLY_FOR_COSTS_DECISION, CostsDecision.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> decideCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("applyForCostsDecision is not present");
    }

    @Test
    void should_throw_exception_if_costs_decision_type_missing() {
        when(asylumCase.read(COSTS_DECISION_TYPE, CostsDecision.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> decideCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("costsDecisionType is not present");
    }

    @Test
    void should_throw_exception_if_upload_costs_order_missing() {
        when(asylumCase.read(COSTS_DECISION_TYPE, CostsDecisionType.class)).thenReturn(Optional.of(CostsDecisionType.WITHOUT_AN_ORAL_HEARING));
        when(asylumCase.read(UPLOAD_COSTS_ORDER)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> decideCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("uploadCostsOrder is not present");
    }

    @Test
    void should_throw_exception_if_costs_oral_hearing_date_missing() {
        when(asylumCase.read(COSTS_DECISION_TYPE, CostsDecisionType.class)).thenReturn(Optional.of(CostsDecisionType.WITH_AN_ORAL_HEARING));
        when(asylumCase.read(COSTS_ORAL_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> decideCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("costsOralHearingDate is not present");
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> decideCostsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideCostsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideCostsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> decideCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}