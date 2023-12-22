package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;


import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CoreCaseDataService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_UPDATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HearingsUpdateHearingRequestConfirmationTest {
    private static final String CASE_REFERENCE = "1111";
    public static final String MANUAL_HEARING_UPDATE_REQUIRED_TEXT =
        "The hearing could not be automatically updated. You must manually update the hearing in the "
            + "[Hearings tab](/cases/case-details/0/hearings)\n\n"
            + "If required, parties will be informed of the changes to the hearing.";

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CoreCaseDataService coreCaseDataService;
    @Mock
    private StartEventResponse startEventResponse;
    HearingsUpdateHearingRequestConfirmation hearingsUpdateHearingRequestConfirmation;
    private String confirmationBody;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        hearingsUpdateHearingRequestConfirmation = new HearingsUpdateHearingRequestConfirmation(coreCaseDataService);
    }

    @Test
    public void should_set_confirmation_body_when_manual_hearing_update_not_required() {
        String confirmationBody = """
                #### What happens next
                The hearing will be updated as directed.
                                        
                If required, parties will be informed of the changes to the hearing.""";
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);

        PostSubmitCallbackResponse callbackResponse =
                hearingsUpdateHearingRequestConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertEquals(confirmationBody, callbackResponse.getConfirmationBody().get());
    }

    @Test
    public void should_set_confirmation_body_when_manual_hearing_update_required() {
        when(asylumCase.read(MANUAL_UPDATE_HEARING_REQUIRED)).thenReturn(Optional.of(YES));
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);

        PostSubmitCallbackResponse callbackResponse =
            hearingsUpdateHearingRequestConfirmation.handle(callback);

        assertNotNull(callbackResponse);
        assertEquals(MANUAL_HEARING_UPDATE_REQUIRED_TEXT, callbackResponse.getConfirmationBody().get());
    }

    @Test
    void should_create_review_interpreter_booking_task() {
        when(caseDetails.getId()).thenReturn(Long.parseLong(CASE_REFERENCE));

        when(coreCaseDataService.startCaseEvent(TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK, CASE_REFERENCE))
            .thenReturn(startEventResponse);
        when(coreCaseDataService.getCaseFromStartedEvent(startEventResponse)).thenReturn(asylumCase);

        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);

        when(asylumCase.read(AsylumCaseFieldDefinition.SHOULD_TRIGGER_REVIEW_INTERPRETER_TASK, YesOrNo.class))
            .thenReturn(Optional.of(YES));

        hearingsUpdateHearingRequestConfirmation.handle(callback);

        verify(coreCaseDataService).triggerSubmitEvent(
            TRIGGER_REVIEW_INTERPRETER_BOOKING_TASK, CASE_REFERENCE, startEventResponse, asylumCase);

    }
}

