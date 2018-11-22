package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.PostSubmitCallbackDispatcher;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization.Deserializer;

@RunWith(MockitoJUnitRunner.class)
public class PostSubmitCallbackControllerTest {

    @Mock private Deserializer<Callback<AsylumCase>> callbackDeserializer;
    @Mock private PostSubmitCallbackDispatcher<AsylumCase> callbackDispatcher;
    @Mock private PostSubmitCallbackResponse callbackResponse;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;

    private PostSubmitCallbackController postSubmitCallbackController;

    @Before
    public void setUp() {
        postSubmitCallbackController =
            new PostSubmitCallbackController(
                callbackDeserializer,
                callbackDispatcher
            );
    }

    @Test
    public void should_deserialize_submitted_callback_then_dispatch_then_return_response() {

        String asylumCaseCallbackSource = "{\"case\":\"data\"}";

        doReturn(callback)
            .when(callbackDeserializer)
            .deserialize(asylumCaseCallbackSource);

        when(callback.getCaseDetails()).thenReturn(caseDetails);

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(callback);

        ResponseEntity<PostSubmitCallbackResponse> actualResponse =
            postSubmitCallbackController.ccdSubmitted(asylumCaseCallbackSource);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(callback);
    }

    @Test
    public void should_not_allow_null_constructor_arguments() {

        assertThatThrownBy(() -> new PostSubmitCallbackController(null, callbackDispatcher))
            .hasMessage("callbackDeserializer must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> new PostSubmitCallbackController(callbackDeserializer, null))
            .hasMessage("callbackDispatcher must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
