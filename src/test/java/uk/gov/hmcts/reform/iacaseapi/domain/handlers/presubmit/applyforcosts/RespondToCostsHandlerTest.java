package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class RespondToCostsHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private String responseExplanation = "explanation";
    private ApplyForCosts applyForCosts;
    @InjectMocks
    private RespondToCostsHandler respondToCostsHandler;
    private List<IdValue<Document>> responseEvidenceDocuments =
            List.of(new IdValue<>("1",
                    new Document("http://localhost/documents/123456",
                            "http://localhost/documents/123456",
                            "DocumentName.pdf")));

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        respondToCostsHandler = new RespondToCostsHandler();

        when(callback.getEvent()).thenReturn(RESPOND_TO_COSTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        applyForCosts = new ApplyForCosts(
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
                "Legal Representative");
        List<IdValue<ApplyForCosts>> existingAppliesForCosts = new ArrayList<>();
        existingAppliesForCosts.add(new IdValue<>("1", applyForCosts));

        List<Value> respondToCostsList = new ArrayList<>();
        respondToCostsList.add(new Value("1", "Costs 1, Wasted costs, 10 Nov 2023"));
        DynamicList dynamicList = new DynamicList(new Value("1", "Costs 1, Wasted costs, 10 Nov 2023"), respondToCostsList);

        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.of(dynamicList));
        when(asylumCase.read(APPLIES_FOR_COSTS)).thenReturn(Optional.of(existingAppliesForCosts));
        when(asylumCase.read(RESPONSE_TO_APPLICATION_TEXT_AREA, String.class)).thenReturn(Optional.of(responseExplanation));
        when(asylumCase.read(RESPONSE_TO_APPLICATION_EVIDENCE)).thenReturn(Optional.of(responseEvidenceDocuments));
        when(asylumCase.read(TYPE_OF_HEARING_OPTION, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(TYPE_OF_HEARING_EXPLANATION, String.class)).thenReturn(Optional.of(responseExplanation));
    }

    @Test
    void should_add_response_evidence_hearingExplanation() {

        PreSubmitCallbackResponse<AsylumCase> response = respondToCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(response);
        assertThat(applyForCosts.getResponseToApplication().equals(responseExplanation));
        assertThat(applyForCosts.getResponseHearingType().equals(YesOrNo.YES));
        assertThat(applyForCosts.getResponseHearingTypeExplanation().equals(responseExplanation));
        assertThat(applyForCosts.getResponseEvidence().equals(responseEvidenceDocuments));
    }

    @Test
    void should_throw_exception_if_responseToApplicationTextArea_missing() {
        when(asylumCase.read(RESPONSE_TO_APPLICATION_TEXT_AREA, String.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> respondToCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("No response to application is present");

    }

    @Test
    void should_throw_exception_if_typeOfHearingOption_missing() {
        when(asylumCase.read(TYPE_OF_HEARING_OPTION, YesOrNo.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> respondToCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("typeOfHearing is not present");

    }

    @Test
    void should_throw_exception_if_typeOfHearingExplanation_missing() {
        when(asylumCase.read(TYPE_OF_HEARING_EXPLANATION, String.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> respondToCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("typeOfHearingExplanation is not present");

    }

    @Test
    void should_throw_exception_if_respondToCostsDynamicList_missing() {
        when(asylumCase.read(RESPOND_TO_COSTS_LIST, DynamicList.class)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> respondToCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("respondToCostsDynamicList is not present");

    }


    @Test
    void handling_should_throw_if_stage_is_incorrect_handle() {
        assertThatThrownBy(() -> respondToCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void handling_should_throw_if_event_is_incorrect_handle() {
        when(callback.getEvent()).thenReturn(END_APPEAL);
        assertThatThrownBy(() -> respondToCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> respondToCostsHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> respondToCostsHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> respondToCostsHandler.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> respondToCostsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}