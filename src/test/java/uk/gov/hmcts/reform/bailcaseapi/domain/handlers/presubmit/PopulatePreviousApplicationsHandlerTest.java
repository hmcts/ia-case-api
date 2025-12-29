package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event.VIEW_PREVIOUS_APPLICATIONS;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.PriorApplication;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.MakeNewApplicationService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PopulatePreviousApplicationsHandlerTest {

    @Mock
    private MakeNewApplicationService makeNewApplicationService;
    @Mock
    private PopulatePreviousApplicationsHandler populatePreviousApplicationsHandler;
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    List<IdValue<PriorApplication>> priorApplications;
    @Mock
    PriorApplication priorApplication;
    @Mock
    BailCase priorCase;

    @Captor
    private ArgumentCaptor<DynamicList> previousApplicationListCaptor;

    @BeforeEach
    void setUp() {
        populatePreviousApplicationsHandler = new PopulatePreviousApplicationsHandler(makeNewApplicationService);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(priorCase.read(BailCaseFieldDefinition.END_APPLICATION_DATE, String.class))
            .thenReturn(Optional.of("2022-06-20"));
        when(priorCase.read(BailCaseFieldDefinition.DECISION_DETAILS_DATE, String.class))
            .thenReturn(Optional.of("2022-06-20"));

    }

    @Test
    void should_error_if_empty_list() {
        when(bailCase.read(BailCaseFieldDefinition.PRIOR_APPLICATIONS)).thenReturn(Optional.empty());
        when(callback.getEvent()).thenReturn(VIEW_PREVIOUS_APPLICATIONS);
        PreSubmitCallbackResponse<BailCase> response =
            populatePreviousApplicationsHandler.handle(ABOUT_TO_START, callback);
        assertNotNull(response);
        assertEquals(1, response.getErrors().size());
        assertEquals(bailCase, response.getData());
        assertTrue(response.getErrors().contains("There is no previous application to view"));
    }

    @Test
    void should_append_details_for_ended_application() {
        String caseData = "{\"endApplicationOutcome\":\"Bail dismissed without a hearing\","
            + "\"applicantGivenNames\":\"John\",\"applicantFamilyName\":\"Smith\","
            + "\"outcomeState\":\"applicationEnded\",\"endApplicationDate\":\"2022-06-20\"}";
        setUpPriorApplication(caseData);

        PreSubmitCallbackResponse<BailCase> response =
            populatePreviousApplicationsHandler.handle(ABOUT_TO_START, callback);

        assertNotNull(response);
        verify(bailCase, times(1))
            .write(eq(BailCaseFieldDefinition.PREVIOUS_APPLICATION_LIST), previousApplicationListCaptor.capture());

        DynamicList previousApplications = previousApplicationListCaptor.getAllValues().get(0);
        assertTrue(previousApplications.getValue().getLabel().contains("Ended 20-06-2022"));
    }

    @Test
    void should_append_details_for_decided_application() {
        String caseData = "{\"recordDecisionType\":\"refused\",\"applicantGivenNames\":\"John\","
            + "\"applicantFamilyName\":\"Smith\",\"decisionDetailsDate\":\"2022-06-20\"}";
        setUpPriorApplication(caseData);

        PreSubmitCallbackResponse<BailCase> response =
            populatePreviousApplicationsHandler.handle(ABOUT_TO_START, callback);

        assertNotNull(response);
        verify(bailCase, times(1))
            .write(eq(BailCaseFieldDefinition.PREVIOUS_APPLICATION_LIST), previousApplicationListCaptor.capture());

        DynamicList previousApplications = previousApplicationListCaptor.getAllValues().get(0);
        assertTrue(previousApplications.getValue().getLabel().contains("Decided 20-06-2022"));
    }


    @Test
    void should_throw_exception_if_decision_details_missing() {
        String caseData = "{\"applicantGivenNames\":\"John\","
            + "\"applicantFamilyName\":\"Smith\",\"decisionDetailsDate\":\"2022-06-20\"}";
        setUpPriorApplication(caseData);

        assertThatThrownBy(() -> populatePreviousApplicationsHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Missing Decision Details")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

    }

    @Test
    void should_throw_exception_if_date_details_missing() {
        String caseData = "{\"recordDecisionType\":\"refused\",\"applicantGivenNames\":\"John\","
            + "\"applicantFamilyName\":\"Smith\"}";
        setUpPriorApplication(caseData);

        when(priorCase.read(BailCaseFieldDefinition.DECISION_DETAILS_DATE, String.class))
            .thenReturn(Optional.empty());
        assertThatThrownBy(() -> populatePreviousApplicationsHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Missing Decision Date")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

    @Test
    void should_handle_allowed_event() {
        for (Event event: Event.values()) {
            for (PreSubmitCallbackStage stage: PreSubmitCallbackStage.values()) {
                when(callback.getEvent()).thenReturn(event);
                boolean canHandle = populatePreviousApplicationsHandler.canHandle(stage, callback);
                if (stage == ABOUT_TO_START && callback.getEvent() == VIEW_PREVIOUS_APPLICATIONS) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> populatePreviousApplicationsHandler
            .canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> populatePreviousApplicationsHandler
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> populatePreviousApplicationsHandler
            .handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> populatePreviousApplicationsHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handler_throws_error_if_cannot_actually_handle() {

        assertThatThrownBy(() -> populatePreviousApplicationsHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.MAKE_NEW_APPLICATION);
        assertThatThrownBy(() -> populatePreviousApplicationsHandler.handle(ABOUT_TO_START, callback))
            .isExactlyInstanceOf(IllegalStateException.class);

    }

    private void setUpPriorApplication(String caseData) {
        priorApplications = List.of(new IdValue<>("1", priorApplication));
        when(priorApplication.getCaseDataJson()).thenReturn(caseData);
        when(bailCase.read(BailCaseFieldDefinition.PRIOR_APPLICATIONS)).thenReturn(Optional.of(priorApplications));
        when(callback.getEvent()).thenReturn(VIEW_PREVIOUS_APPLICATIONS);
        when(makeNewApplicationService.getBailCaseFromString(caseData)).thenReturn(priorCase);
    }

}
