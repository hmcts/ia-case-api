package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE_ACTION_AVAILABLE;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class RequestResponseAmendHandlerTest {

    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Mock private AsylumCase asylumCase;

    @Captor private ArgumentCaptor<String> asylumCaseValuesArgumentCaptor;
    @Captor private ArgumentCaptor<AsylumCaseFieldDefinition> asylumExtractorCaptor;

    private RequestResponseAmendHandler requestResponseAmendHandler;

    @Before
    public void setUp() {
        requestResponseAmendHandler = new RequestResponseAmendHandler();
    }

    @Test
    public void should_prepare_ho_appeal_response_action_field() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.REQUEST_RESPONSE_AMEND);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            requestResponseAmendHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(asylumCase, callbackResponse.getData());

        verify(asylumCase, times(1)).write(asylumExtractorCaptor.capture(), asylumCaseValuesArgumentCaptor.capture());

        List<AsylumCaseFieldDefinition> extractors = asylumExtractorCaptor.getAllValues();
        List<String> asylumCaseValues = asylumCaseValuesArgumentCaptor.getAllValues();

        verify(asylumCase, times(1)).write(UPLOAD_HOME_OFFICE_APPEAL_RESPONSE_ACTION_AVAILABLE, YesOrNo.YES);
    }

    @Test
    public void handling_should_throw_if_cannot_actual_handle() {
        assertThatThrownBy(() -> requestResponseAmendHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> requestResponseAmendHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = requestResponseAmendHandler.canHandle(callbackStage, callback);

                if (event == Event.REQUEST_RESPONSE_AMEND
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    public void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> requestResponseAmendHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestResponseAmendHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestResponseAmendHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> requestResponseAmendHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
