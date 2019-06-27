package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit.*;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class PostSubmitCallbackDispatcherTest {

    @Mock private PostSubmitCallbackHandler<CaseData> handler1;
    @Mock private PostSubmitCallbackHandler<CaseData> handler2;
    @Mock private PostSubmitCallbackHandler<CaseData> handler3;
    @Mock private Callback<CaseData> callback;
    @Mock private PostSubmitCallbackResponse response;

    private PostSubmitCallbackDispatcher<CaseData> postSubmitCallbackDispatcher;

    @Before
    public void setUp() {
        postSubmitCallbackDispatcher = new PostSubmitCallbackDispatcher<>(
            Arrays.asList(
                handler1,
                handler2,
                handler3
            )
        );
    }

    @Test
    public void should_dispatch_callback_to_first_eligible_handler_collecting_confirmation() {

        Optional<String> expectedConfirmationHeader = Optional.of("header");
        Optional<String> expectedConfirmationBody = Optional.of("body");

        when(response.getConfirmationHeader()).thenReturn(expectedConfirmationHeader);
        when(response.getConfirmationBody()).thenReturn(expectedConfirmationBody);

        when(handler1.canHandle(callback)).thenReturn(false);

        when(handler2.canHandle(callback)).thenReturn(true);
        when(handler2.handle(callback)).thenReturn(response);

        PostSubmitCallbackResponse callbackResponse =
            postSubmitCallbackDispatcher.handle(callback);

        assertNotNull(callbackResponse);
        assertEquals(expectedConfirmationHeader, callbackResponse.getConfirmationHeader());
        assertEquals(expectedConfirmationBody, callbackResponse.getConfirmationBody());

        verify(handler1, times(1)).canHandle(callback);
        verify(handler1, times(0)).handle(callback);

        verify(handler2, times(1)).canHandle(callback);
        verify(handler2, times(1)).handle(callback);

        verify(handler3, times(0)).canHandle(callback);
        verify(handler3, times(0)).handle(callback);
    }

    @Test
    public void should_not_error_if_no_handlers_are_provided() {

        PostSubmitCallbackDispatcher<CaseData> postSubmitCallbackDispatcher =
            new PostSubmitCallbackDispatcher<>(Collections.emptyList());

        try {

            PostSubmitCallbackResponse callbackResponse =
                postSubmitCallbackDispatcher.handle(callback);

            assertNotNull(callbackResponse);
            assertEquals(Optional.empty(), callbackResponse.getConfirmationHeader());
            assertEquals(Optional.empty(), callbackResponse.getConfirmationBody());

        } catch (Exception e) {
            fail("Should not have thrown any exception");
        }
    }

    @Test
    public void should_not_allow_null_handlers() {

        assertThatThrownBy(() -> new PostSubmitCallbackDispatcher<>(null))
            .hasMessage("callbackHandlers must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> postSubmitCallbackDispatcher.handle(null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_sort_handlers_by_name() {
        PostSubmitCallbackHandler<AsylumCase> h1 = new AppealResponseAddedConfirmation();
        PostSubmitCallbackHandler<AsylumCase> h2 = new BuildCaseConfirmation();
        PostSubmitCallbackHandler<AsylumCase> h3 = new GenerateHearingBundleConfirmation();
        PostSubmitCallbackHandler<AsylumCase> h4 = new RequestCaseEditConfirmation();
        PostSubmitCallbackHandler<AsylumCase> h5 = new SendDirectionConfirmation();
        PostSubmitCallbackHandler<AsylumCase> h6 = new UploadRespondentEvidenceConfirmation();

        PostSubmitCallbackDispatcher<AsylumCase> dispatcher = new PostSubmitCallbackDispatcher<>(
            Arrays.asList(
                h4,
                h2,
                h3,
                h1,
                h6,
                h5
            )
        );

        List<PostSubmitCallbackHandler<AsylumCase>> sortedDispatcher =
            (List<PostSubmitCallbackHandler<AsylumCase>>) ReflectionTestUtils.getField(dispatcher, "sortedCallbackHandlers");

        assertEquals(6, sortedDispatcher.size());
        assertEquals(h1, sortedDispatcher.get(0));
        assertEquals(h2, sortedDispatcher.get(1));
        assertEquals(h3, sortedDispatcher.get(2));
        assertEquals(h4, sortedDispatcher.get(3));
        assertEquals(h5, sortedDispatcher.get(4));
        assertEquals(h6, sortedDispatcher.get(5));
    }
}
