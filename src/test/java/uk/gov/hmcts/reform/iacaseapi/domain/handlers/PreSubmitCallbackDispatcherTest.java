package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class PreSubmitCallbackDispatcherTest {

    @Mock private PreSubmitCallbackHandler<CaseData> handler1;
    @Mock private PreSubmitCallbackHandler<CaseData> handler2;
    @Mock private PreSubmitCallbackHandler<CaseData> handler3;
    @Mock private Callback<CaseData> callback;
    @Mock private CaseDetails<CaseData> caseDetails;
    @Mock private CaseData caseData;
    @Mock private PreSubmitCallbackResponse<CaseData> response1;
    @Mock private PreSubmitCallbackResponse<CaseData> response2;
    @Mock private PreSubmitCallbackResponse<CaseData> response3;

    private PreSubmitCallbackDispatcher<CaseData> preSubmitCallbackDispatcher;

    @Before
    public void setUp() {
        preSubmitCallbackDispatcher = new PreSubmitCallbackDispatcher<>(
            Arrays.asList(
                handler1,
                handler2,
                handler3
            )
        );
    }

    @Test
    public void should_dispatch_callback_to_handlers_according_to_priority_collecting_errors() {

        Set<String> expectedErrors =
            ImmutableSet.of("error1", "error2", "error3", "error4");

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(caseData);

            when(response1.getErrors()).thenReturn(ImmutableSet.of("error1"));
            when(response2.getErrors()).thenReturn(ImmutableSet.of("error2", "error3"));
            when(response3.getErrors()).thenReturn(ImmutableSet.of("error4"));

            when(handler1.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
            when(handler1.canHandle(callbackStage, callback)).thenReturn(true);
            when(handler1.handle(callbackStage, callback)).thenReturn(response1);

            when(handler2.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
            when(handler2.canHandle(callbackStage, callback)).thenReturn(true);
            when(handler2.handle(callbackStage, callback)).thenReturn(response2);

            when(handler3.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
            when(handler3.canHandle(callbackStage, callback)).thenReturn(true);
            when(handler3.handle(callbackStage, callback)).thenReturn(response3);

            PreSubmitCallbackResponse<CaseData> callbackResponse =
                preSubmitCallbackDispatcher.handle(callbackStage, callback);

            assertNotNull(callbackResponse);
            assertEquals(caseData, callbackResponse.getData());
            assertThat(callbackResponse.getErrors(), is(expectedErrors));

            InOrder inOrder = inOrder(handler1, handler3, handler2);

            inOrder.verify(handler1, times(1)).canHandle(callbackStage, callback);
            inOrder.verify(handler1, times(1)).handle(callbackStage, callback);

            inOrder.verify(handler3, times(1)).canHandle(callbackStage, callback);
            inOrder.verify(handler3, times(1)).handle(callbackStage, callback);

            inOrder.verify(handler2, times(1)).canHandle(callbackStage, callback);
            inOrder.verify(handler2, times(1)).handle(callbackStage, callback);

            reset(handler1, handler2, handler3);
        }
    }

    @Test
    public void should_only_dispatch_callback_to_handlers_that_can_handle_it() {

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(caseData);

            when(handler1.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
            when(handler1.canHandle(callbackStage, callback)).thenReturn(false);
            when(handler1.handle(callbackStage, callback)).thenReturn(response1);

            when(handler2.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
            when(handler2.canHandle(callbackStage, callback)).thenReturn(false);
            when(handler2.handle(callbackStage, callback)).thenReturn(response2);

            when(handler3.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
            when(handler3.canHandle(callbackStage, callback)).thenReturn(true);
            when(handler3.handle(callbackStage, callback)).thenReturn(response3);

            PreSubmitCallbackResponse<CaseData> callbackResponse =
                preSubmitCallbackDispatcher.handle(callbackStage, callback);

            assertNotNull(callbackResponse);
            assertEquals(caseData, callbackResponse.getData());
            assertTrue(callbackResponse.getErrors().isEmpty());

            verify(handler1, times(1)).canHandle(callbackStage, callback);
            verify(handler1, times(0)).handle(callbackStage, callback);

            verify(handler2, times(1)).canHandle(callbackStage, callback);
            verify(handler2, times(0)).handle(callbackStage, callback);

            verify(handler3, times(1)).canHandle(callbackStage, callback);
            verify(handler3, times(1)).handle(callbackStage, callback);

            reset(handler1, handler2, handler3);
        }
    }

    @Test
    public void should_not_error_if_no_handlers_are_provided() {

        PreSubmitCallbackDispatcher<CaseData> preSubmitCallbackDispatcher =
            new PreSubmitCallbackDispatcher<>(Collections.emptyList());

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            try {

                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(caseData);

                PreSubmitCallbackResponse<CaseData> callbackResponse =
                    preSubmitCallbackDispatcher
                        .handle(callbackStage, callback);

                assertNotNull(callbackResponse);
                assertEquals(caseData, callbackResponse.getData());
                assertTrue(callbackResponse.getErrors().isEmpty());

            } catch (Exception e) {
                fail("Should not have thrown any exception");
            }
        }
    }

    @Test
    public void should_not_allow_null_handlers() {

        assertThatThrownBy(() -> new PreSubmitCallbackDispatcher<>(null))
            .hasMessage("callbackHandlers must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    public void should_not_allow_null_parameters() {

        assertThatThrownBy(() -> preSubmitCallbackDispatcher.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> preSubmitCallbackDispatcher.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
