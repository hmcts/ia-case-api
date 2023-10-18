package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HearingsUpdateHearingRequestConfirmationTest {
    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    HearingsUpdateHearingRequestConfirmation hearingsUpdateHearingRequestConfirmation;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        hearingsUpdateHearingRequestConfirmation = new HearingsUpdateHearingRequestConfirmation();
    }

    @Test
    public void should_delegate_to_hearings_api_start_event_when_update_hearings_is_null() {
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
}

