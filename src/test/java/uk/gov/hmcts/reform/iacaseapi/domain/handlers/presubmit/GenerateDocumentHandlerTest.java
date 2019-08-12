package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.immutableEnumSet;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;

import com.google.common.collect.ImmutableSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Application;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentGenerator;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class GenerateDocumentHandlerTest {

    @Mock private DocumentGenerator<AsylumCase> documentGenerator;
    @Mock private Callback<AsylumCase> callback;
    @Mock private CaseDetails<AsylumCase> caseDetails;
    @Captor private ArgumentCaptor<List<IdValue<Application>>> applicationsCaptor;

    private State state = State.UNKNOWN;
    private List<IdValue<Application>> applications = newArrayList(new IdValue<>("1", new Application(
        Collections.emptyList(),
        "Legal representative",
        "Expedite",
        "applicationReason",
        "30/01/2019",
        "Granted",
        "decisionReason",
        "31/01/2019",
        "In progress"
    )));

    private GenerateDocumentHandler generateDocumentHandler;

    @Before
    public void setUp() {

        generateDocumentHandler =
            new GenerateDocumentHandler(
                true,
                true,
                documentGenerator
            );
    }

    @Test
    public void should_generate_document_and_update_the_case() {

        Arrays.asList(
            SUBMIT_APPEAL,
            SUBMIT_CASE,
            LIST_CASE,
            EDIT_CASE_LISTING,
            GENERATE_HEARING_BUNDLE,
            GENERATE_DECISION_AND_REASONS
        ).forEach(event -> {

            AsylumCase expectedUpdatedCase = mock(AsylumCase.class);

            when(callback.getEvent()).thenReturn(event);
            if (event.equals(EDIT_CASE_LISTING)) {
                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getState()).thenReturn(state);
                when(expectedUpdatedCase.read(APPLICATION_EDIT_LISTING_EXISTS, String.class)).thenReturn(Optional.of("Yes"));
                when(expectedUpdatedCase.read(APPLICATIONS)).thenReturn(Optional.of(applications));
            }

            when(documentGenerator.generate(callback)).thenReturn(expectedUpdatedCase);

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(expectedUpdatedCase, callbackResponse.getData());

            verify(documentGenerator, times(1)).generate(callback);

            if (event.equals(EDIT_CASE_LISTING)) {
                verify(expectedUpdatedCase).clear(DISABLE_OVERVIEW_PAGE);
                verify(expectedUpdatedCase).clear(APPLICATION_EDIT_LISTING_EXISTS);
                verify(expectedUpdatedCase).write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, state);
                verify(expectedUpdatedCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
                assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());
            }

            reset(callback);
            reset(documentGenerator);
        });
    }

    @Test
    public void should_be_handled_at_latest_point() {
        assertEquals(DispatchPriority.LATEST, generateDocumentHandler.getDispatchPriority());
    }

    @Test
    public void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    public void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = generateDocumentHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    &&
                    Arrays.asList(
                        SUBMIT_APPEAL,
                        SUBMIT_CASE,
                        LIST_CASE,
                        EDIT_CASE_LISTING,
                        GENERATE_HEARING_BUNDLE,
                        GENERATE_DECISION_AND_REASONS,
                        SEND_DECISION_AND_REASONS
                    ).contains(event)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse("failed callback: " + callbackStage + ", failed event " + event, canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    public void it_cannot_handle_callback_if_docmosis_not_enabled() {

        generateDocumentHandler =
            new GenerateDocumentHandler(
                false,
                true,
                documentGenerator
            );

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = generateDocumentHandler.canHandle(callbackStage, callback);

                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    public void it_cannot_handle_generate_if_em_stitching_not_enabled() {

        generateDocumentHandler =
            new GenerateDocumentHandler(
                true,
                false,
                documentGenerator
            );

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = generateDocumentHandler.canHandle(callbackStage, callback);

                ImmutableSet<Event> eventsThatDontRequireStitching =
                    immutableEnumSet(
                        SUBMIT_APPEAL,
                        LIST_CASE,
                        GENERATE_DECISION_AND_REASONS,
                        EDIT_CASE_LISTING,
                        SEND_DECISION_AND_REASONS);

                if (callbackStage.equals(PreSubmitCallbackStage.ABOUT_TO_SUBMIT) && (eventsThatDontRequireStitching.contains(event))) {
                    assertTrue(canHandle);
                } else if (event.equals(GENERATE_HEARING_BUNDLE)) {
                    assertFalse(canHandle);
                } else {
                    assertFalse("event: " + event + ", stage: " + callbackStage, canHandle);
                }

            }

            reset(callback);
        }

    }

    @Test
    public void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> generateDocumentHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateDocumentHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateDocumentHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
