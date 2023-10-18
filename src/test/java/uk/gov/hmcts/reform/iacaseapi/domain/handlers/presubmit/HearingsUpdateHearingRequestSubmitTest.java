package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;


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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.AsylumCaseCallbackApiDelegator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class HearingsUpdateHearingRequestSubmitTest {
    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private final String hearingsApiEndpoint = "hearings-end-point";
    private final String aboutToSubmitPath = "/about-to-ccdAboutToSubmit";
    @Mock
    private AsylumCaseCallbackApiDelegator asylumCaseCallbackApiDelegator;
    HearingsUpdateHearingRequestSubmit hearingsUpdateHearingRequestSubmit;

    @BeforeEach
    public void setUp() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getCaseDetails().getCaseData()).thenReturn(asylumCase);

        hearingsUpdateHearingRequestSubmit =
                new HearingsUpdateHearingRequestSubmit(
                        asylumCaseCallbackApiDelegator,
                        hearingsApiEndpoint,
                        aboutToSubmitPath
                );
    }

    @Test
    public void should_delegate_to_hearings_api_submit_event() {
        when(callback.getEvent()).thenReturn(UPDATE_HEARING_REQUEST);
        when(asylumCaseCallbackApiDelegator.delegate(callback, hearingsApiEndpoint + aboutToSubmitPath))
                .thenReturn(asylumCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                hearingsUpdateHearingRequestSubmit.handle(ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        verify(asylumCaseCallbackApiDelegator, times(1))
                .delegate(callback, hearingsApiEndpoint + aboutToSubmitPath);
        assertEquals(asylumCase, callbackResponse.getData());
    }


}

