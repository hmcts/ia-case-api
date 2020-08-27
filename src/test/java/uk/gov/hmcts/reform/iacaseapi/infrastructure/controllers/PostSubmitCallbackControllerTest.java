package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.PostSubmitCallbackDispatcher;

@ExtendWith(MockitoExtension.class)
class PostSubmitCallbackControllerTest {

    @Mock PostSubmitCallbackDispatcher<AsylumCase> callbackDispatcher;
    @Mock PostSubmitCallbackResponse callbackResponse;
    @Mock Callback<AsylumCase> callback;
    @Mock CaseDetails<AsylumCase> caseDetails;

    PostSubmitCallbackController postSubmitCallbackController;

    @BeforeEach
    void setUp() {

        postSubmitCallbackController =
            new PostSubmitCallbackController(
                callbackDispatcher
            );
    }

    @Test
    void should_dispatch_callback_then_return_response() {

        when(callback.getCaseDetails()).thenReturn(caseDetails);

        doReturn(callbackResponse)
            .when(callbackDispatcher)
            .handle(callback);

        ResponseEntity<PostSubmitCallbackResponse> actualResponse =
            postSubmitCallbackController.ccdSubmitted(callback);

        assertNotNull(actualResponse);

        verify(callbackDispatcher, times(1)).handle(callback);
    }

    @Test
    void should_not_allow_null_constructor_arguments() {

        assertThatThrownBy(() -> new PostSubmitCallbackController(null))
            .hasMessage("callbackDispatcher must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
