package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADD_EVIDENCE_FOR_COSTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.END_APPEAL;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
class AdditionalEvidenceForCostsHandlerTest {
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
    @InjectMocks
    private AdditionalEvidenceForCostsHandler additionalEvidenceForCostsHandler;
    private static List<IdValue<Document>> evidenceDocuments =
        List.of(new IdValue<>("1",
            new Document("http://localhost/documents/123456",
                "http://localhost/documents/123456",
                "DocumentName.pdf")));

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        additionalEvidenceForCostsHandler = new AdditionalEvidenceForCostsHandler(applyForCostsProvider);

        when(callback.getEvent()).thenReturn(ADD_EVIDENCE_FOR_COSTS);
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
            "Legal representative");
        List<IdValue<ApplyForCosts>> existingAppliesForCosts = new ArrayList<>();
        existingAppliesForCosts.add(new IdValue<>("1", applyForCosts));

        List<Value> respondToCostsList = new ArrayList<>();
        respondToCostsList.add(new Value("1", "Costs 1, Wasted costs, 10 Nov 2023"));
        DynamicList dynamicList = new DynamicList(new Value("1", "Costs 1, Wasted costs, 10 Nov 2023"), respondToCostsList);

        when(asylumCase.read(ADD_EVIDENCE_FOR_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(existingAppliesForCosts));
        when(asylumCase.read(ADDITIONAL_EVIDENCE_FOR_COSTS)).thenReturn(Optional.of(evidenceDocuments));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Home office", "Legal representative"})
    void should_add_additional_evidence(String role) {
        when(applyForCostsProvider.getLoggedUserRole()).thenReturn(role);
        PreSubmitCallbackResponse<AsylumCase> response = additionalEvidenceForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        if (role.equals(UserRoleLabel.HOME_OFFICE_GENERIC.toString())) {
            assertThat(applyForCosts.getApplicantAdditionalEvidence().equals(evidenceDocuments));
        } else if (role.equals(UserRoleLabel.LEGAL_REPRESENTATIVE.toString())) {
            assertThat(applyForCosts.getRespondentAdditionalEvidence().equals(evidenceDocuments));
        }
    }

    @Test
    void should_throw_exception_if_list_for_evidences_is_missing() {
        when(asylumCase.read(ADD_EVIDENCE_FOR_COSTS_LIST, DynamicList.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> additionalEvidenceForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("addEvidenceForCostsList is not present");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Home office", "Legal representative"})
    void should_throw_exception_if_additional_evidences_is_missing(String role) {
        when(applyForCostsProvider.getLoggedUserRole()).thenReturn(role);
        when(asylumCase.read(ADDITIONAL_EVIDENCE_FOR_COSTS)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> additionalEvidenceForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("evidenceDocuments are not present");
    }

    @Test
    void handling_should_throw_if_stage_is_incorrect_handle() {
        assertThatThrownBy(() -> additionalEvidenceForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_event_is_incorrect_handle() {
        when(callback.getEvent()).thenReturn(END_APPEAL);
        assertThatThrownBy(() -> additionalEvidenceForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> additionalEvidenceForCostsHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> additionalEvidenceForCostsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> additionalEvidenceForCostsHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> additionalEvidenceForCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}