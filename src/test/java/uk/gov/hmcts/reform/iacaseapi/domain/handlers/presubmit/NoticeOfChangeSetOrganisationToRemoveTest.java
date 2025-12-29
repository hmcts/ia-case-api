package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CallbackApiDelegator;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class NoticeOfChangeSetOrganisationToRemoveTest {

    private static final String BASE_URL = "https://localhost:8080";
    private static final String API_PATH = "/api/path";
    private NoticeOfChangeSetOrganisationToRemove noticeOfChangeSetOrganisationToRemove;

    @Mock
    private CallbackApiDelegator apiDelegator;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;


    @BeforeEach
    public void setUp() throws Exception {
        noticeOfChangeSetOrganisationToRemove = new NoticeOfChangeSetOrganisationToRemove(
                apiDelegator,
                BASE_URL,
                API_PATH);
    }

    @Test
    public void calls_setOrganisationToRemove_and_returns_result() {
        AsylumCase sentCase = new AsylumCase();
        AsylumCase returnedCase = new AsylumCase();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(sentCase);
        when(callback.getEvent()).thenReturn(Event.REMOVE_LEGAL_REPRESENTATIVE);
        when(apiDelegator.delegate(any(), anyString())).thenReturn(returnedCase);

        PreSubmitCallbackResponse<AsylumCase> response = noticeOfChangeSetOrganisationToRemove.handle(
                PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
                callback
        );

        verify(apiDelegator, times(1)).delegate(callback, BASE_URL + API_PATH);
        assertEquals(returnedCase, response.getData());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = noticeOfChangeSetOrganisationToRemove.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && (event == Event.REMOVE_REPRESENTATION
                            || event == Event.REMOVE_LEGAL_REPRESENTATIVE)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
            reset(callback);
        }
    }
}
