package uk.gov.hmcts.reform.bailcaseapi.infrastructure;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.eventvalidation.EventValid;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.eventvalidation.EventValidCheckers;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.CcdEventAuthorizor;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class PreSubmitCallbackDispatcherTest {

    @Mock
    private CcdEventAuthorizor ccdEventAuthorizor;

    @Mock
    private PreSubmitCallbackHandler<CaseData> handler1;

    @Mock
    private PreSubmitCallbackHandler<CaseData> handler2;

    @Mock
    private PreSubmitCallbackHandler<CaseData> handler3;

    @Mock
    private PreSubmitCallbackStateHandler<CaseData> stateHandler;

    @Mock
    private EventValidCheckers<CaseData> eventValidChecker;

    @Mock
    private Callback<CaseData> callback;

    @Mock
    private CaseDetails<CaseData> caseDetails;

    @Mock
    private CaseData caseData;

    @Mock
    private CaseData caseDataMutation1;

    @Mock
    private CaseData caseDataMutation2;

    @Mock
    private CaseData caseDataMutation3;

    @Mock
    private PreSubmitCallbackResponse<CaseData> response1;

    @Mock
    private PreSubmitCallbackResponse<CaseData> response2;

    @Mock
    private PreSubmitCallbackResponse<CaseData> response3;

    private PreSubmitCallbackDispatcher<CaseData> preSubmitCallbackDispatcher;

    @BeforeEach
    public void setUp() {
        preSubmitCallbackDispatcher = new PreSubmitCallbackDispatcher(
            ccdEventAuthorizor,
            Arrays.asList(
                handler1,
                handler2,
                handler3
            ),
            eventValidChecker,
            Arrays.asList(stateHandler)
        );
        when(eventValidChecker.check(any(Callback.class))).thenReturn(new EventValid());
    }

    @Test
    void check_adds_errors_for_invalid_for_journey_type() {
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(caseData);
        when(eventValidChecker.check(any(Callback.class))).thenReturn(new EventValid("Invalid reason"));

        PreSubmitCallbackResponse<CaseData> callbackResponse =
            preSubmitCallbackDispatcher.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertThat(callbackResponse.getErrors()).contains("Invalid reason");
    }

    @Test
    void check_callback_to_handlers_according_to_priority_with_errors() {

        Set<String> expectedErrors =
            ImmutableSet.of("error1", "error2", "error3", "error4");

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            when(caseDetails.getCaseData()).thenReturn(caseData);

            when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
            when(callback.getCaseDetails()).thenReturn(caseDetails);

            when(response1.getData()).thenReturn(caseDataMutation1);
            when(response1.getErrors()).thenReturn(ImmutableSet.of("error1"));

            when(response2.getData()).thenReturn(caseDataMutation2);
            when(response2.getErrors()).thenReturn(ImmutableSet.of("error2", "error3"));

            when(response3.getData()).thenReturn(caseDataMutation3);
            when(response3.getErrors()).thenReturn(ImmutableSet.of("error4"));

            when(handler1.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
            when(handler1.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(true);
            when(handler1.handle(eq(callbackStage), any(Callback.class))).thenReturn(response1);

            when(handler2.getDispatchPriority()).thenReturn(DispatchPriority.LATE);
            when(handler2.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(true);
            when(handler2.handle(eq(callbackStage), any(Callback.class))).thenReturn(response2);

            when(handler3.getDispatchPriority()).thenReturn(DispatchPriority.EARLY);
            when(handler3.canHandle(eq(callbackStage), any(Callback.class))).thenReturn(true);
            when(handler3.handle(eq(callbackStage), any(Callback.class))).thenReturn(response3);

            PreSubmitCallbackResponse<CaseData> callbackResponse =
                preSubmitCallbackDispatcher.handle(callbackStage, callback);

            assertNotNull(callbackResponse);
            assertEquals(caseDataMutation2, callbackResponse.getData());
            assertThat(callbackResponse.getErrors()).containsExactlyInAnyOrderElementsOf(expectedErrors);

            verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.START_APPLICATION);

            InOrder inOrder = inOrder(handler1, handler3, handler2);

            inOrder.verify(handler1, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            inOrder.verify(handler1, times(1)).handle(eq(callbackStage), any(Callback.class));

            inOrder.verify(handler3, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            inOrder.verify(handler3, times(1)).handle(eq(callbackStage), any(Callback.class));

            inOrder.verify(handler2, times(1)).canHandle(eq(callbackStage), any(Callback.class));
            inOrder.verify(handler2, times(1)).handle(eq(callbackStage), any(Callback.class));

            reset(ccdEventAuthorizor, handler1, handler2, handler3);

        }
    }

    @Test
    void should_not_error_if_no_handlers_are_provided() {

        PreSubmitCallbackDispatcher<CaseData> preSubmitCallbackDispatcher =
            new PreSubmitCallbackDispatcher(ccdEventAuthorizor, Collections.emptyList(),
                                            eventValidChecker, Collections.emptyList());

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            try {
                when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
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
    void should_not_dispatch_to_handlers_if_user_not_authorized_for_event() {

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            when(callback.getEvent()).thenReturn(Event.UPLOAD_BAIL_SUMMARY);

            doThrow(AccessDeniedException.class)
                .when(ccdEventAuthorizor)
                .throwIfNotAuthorized(Event.UPLOAD_BAIL_SUMMARY);

            Assertions.assertThatThrownBy(() -> preSubmitCallbackDispatcher.handle(callbackStage, callback))
                .isExactlyInstanceOf(AccessDeniedException.class);

            verify(ccdEventAuthorizor, times(1)).throwIfNotAuthorized(Event.UPLOAD_BAIL_SUMMARY);

            verify(handler1, never()).canHandle(any(), any());
            verify(handler1, never()).handle(any(), any());
            verify(handler2, never()).canHandle(any(), any());
            verify(handler2, never()).handle(any(), any());
            verify(handler3, never()).canHandle(any(), any());
            verify(handler3, never()).handle(any(), any());

            reset(ccdEventAuthorizor, handler1, handler2, handler3);
        }
    }

    @Test
    void should_not_allow_null_handlers() {

        assertThatThrownBy(() -> new PreSubmitCallbackDispatcher<>(ccdEventAuthorizor, null, eventValidChecker, null))
            .hasMessage("callbackHandlers must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> preSubmitCallbackDispatcher.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }


}
