package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.immutableEnumSet;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATION_EDIT_LISTING_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATION_WITHDRAW_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DISABLE_OVERVIEW_PAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_DECISION_ALLOWED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;

import com.google.common.collect.ImmutableSet;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Application;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationType;
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

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class GenerateDocumentHandlerTest {

    @Mock
    private DocumentGenerator<AsylumCase> documentGenerator;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private DateProvider dateProvider;
    @Captor
    private ArgumentCaptor<List<IdValue<Application>>> applicationsCaptor;

    private State state = State.UNKNOWN;
    private String applicationSupplier = "Legal representative";
    private String applicationReason = "applicationReason";
    private String applicationDate = "30/01/2019";
    private String applicationDecision = "Granted";
    private String applicationDecisionReason = "Granted";
    private String applicationDateOfDecision = "31/01/2019";
    private String applicationStatus = "In progress";

    private List<IdValue<Application>> applications = newArrayList(new IdValue<>("1", new Application(
        Collections.emptyList(),
        applicationSupplier,
        ApplicationType.EXPEDITE.toString(),
        applicationReason,
        applicationDate,
        applicationDecision,
        applicationDecisionReason,
        applicationDateOfDecision,
        applicationStatus
    )));

    private LocalDate now = LocalDate.now();

    private GenerateDocumentHandler generateDocumentHandler;

    @BeforeEach
    public void setUp() {

        generateDocumentHandler =
            new GenerateDocumentHandler(
                true,
                true,
                documentGenerator,
                dateProvider,
                true);
    }

    @Test
    void should_generate_document_and_update_the_case() {

        Arrays.asList(
            SUBMIT_APPEAL,
            SUBMIT_CASE,
            LIST_CASE,
            EDIT_CASE_LISTING,
            GENERATE_DECISION_AND_REASONS,
            GENERATE_HEARING_BUNDLE,
            CUSTOMISE_HEARING_BUNDLE,
            SEND_DECISION_AND_REASONS,
            ADJOURN_HEARING_WITHOUT_DATE,
            END_APPEAL,
            SUBMIT_CMA_REQUIREMENTS,
            LIST_CMA,
            END_APPEAL,
            END_APPEAL_AUTOMATICALLY,
            EDIT_APPEAL_AFTER_SUBMIT,
            GENERATE_UPPER_TRIBUNAL_BUNDLE,
            SUBMIT_REASONS_FOR_APPEAL,
            SUBMIT_CLARIFYING_QUESTION_ANSWERS
        ).forEach(event -> {

            AsylumCase expectedUpdatedCase = mock(AsylumCase.class);

            when(callback.getEvent()).thenReturn(event);
            if (event.equals(EDIT_CASE_LISTING)) {
                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getState()).thenReturn(state);
                when(expectedUpdatedCase.read(APPLICATION_EDIT_LISTING_EXISTS, String.class))
                    .thenReturn(Optional.of("Yes"));
                when(expectedUpdatedCase.read(APPLICATIONS)).thenReturn(Optional.of(applications));
            }

            if (event.equals(SEND_DECISION_AND_REASONS)) {
                when(expectedUpdatedCase.read(IS_DECISION_ALLOWED, AppealDecision.class))
                    .thenReturn(Optional.of(AppealDecision.ALLOWED));
                when(dateProvider.now()).thenReturn(now);
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

            if (event.equals(SEND_DECISION_AND_REASONS)) {
                verify(expectedUpdatedCase).write(APPEAL_DECISION, "Allowed");
                verify(expectedUpdatedCase).write(APPEAL_DATE, now.toString());
            }

            reset(callback);
            reset(documentGenerator);
        });
    }

    @Test
    void should_handle_edit_listing_with_withdrawn() {

        Arrays.asList(
            ApplicationType.ADJOURN,
            ApplicationType.TRANSFER,
            ApplicationType.EXPEDITE
        ).forEach(applicationType -> {

            List<IdValue<Application>> expectedApplications = newArrayList(new IdValue<>("1", new Application(
                Collections.emptyList(),
                applicationSupplier,
                applicationType.toString(),
                applicationReason,
                applicationDate,
                applicationDecision,
                applicationDecisionReason,
                applicationDateOfDecision,
                applicationStatus
            )));

            AsylumCase expectedUpdatedCase = mock(AsylumCase.class);
            when(callback.getEvent()).thenReturn(EDIT_CASE_LISTING);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getState()).thenReturn(state);
            when(expectedUpdatedCase.read(APPLICATION_EDIT_LISTING_EXISTS, String.class))
                .thenReturn(Optional.of("Yes"));
            when(expectedUpdatedCase.read(APPLICATION_WITHDRAW_EXISTS, String.class)).thenReturn(Optional.of("Yes"));
            when(expectedUpdatedCase.read(APPLICATIONS)).thenReturn(Optional.of(expectedApplications));

            when(documentGenerator.generate(callback)).thenReturn(expectedUpdatedCase);

            PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

            assertNotNull(callbackResponse);
            assertEquals(expectedUpdatedCase, callbackResponse.getData());

            verify(documentGenerator, times(1)).generate(callback);

            verify(expectedUpdatedCase).clear(APPLICATION_EDIT_LISTING_EXISTS);
            verify(expectedUpdatedCase).write(eq(APPLICATIONS), applicationsCaptor.capture());
            assertEquals("Completed", applicationsCaptor.getValue().get(0).getValue().getApplicationStatus());

            reset(documentGenerator);
        });
    }

    @Test
    void should_be_handled_at_latest_point() {
        assertEquals(DispatchPriority.LATEST, generateDocumentHandler.getDispatchPriority());
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = generateDocumentHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    &&
                    Arrays.asList(
                        SUBMIT_APPEAL,
                        SUBMIT_CASE,
                        DRAFT_HEARING_REQUIREMENTS,
                        UPDATE_HEARING_REQUIREMENTS,
                        LIST_CASE,
                        EDIT_CASE_LISTING,
                        GENERATE_DECISION_AND_REASONS,
                        GENERATE_HEARING_BUNDLE,
                        CUSTOMISE_HEARING_BUNDLE,
                        SEND_DECISION_AND_REASONS,
                        ADJOURN_HEARING_WITHOUT_DATE,
                        END_APPEAL,
                        SUBMIT_CMA_REQUIREMENTS,
                        LIST_CMA,
                        END_APPEAL,
                        END_APPEAL_AUTOMATICALLY,
                        EDIT_APPEAL_AFTER_SUBMIT,
                        GENERATE_UPPER_TRIBUNAL_BUNDLE,
                        SUBMIT_REASONS_FOR_APPEAL,
                        SUBMIT_CLARIFYING_QUESTION_ANSWERS
                    ).contains(event)) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle, "failed callback: " + callbackStage + ", failed event " + event);
                }
            }

            reset(callback);
        }
    }

    @Test
    void it_cannot_handle_callback_if_docmosis_not_enabled() {

        generateDocumentHandler =
            new GenerateDocumentHandler(
                false,
                true,
                documentGenerator,
                dateProvider,
                true);

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
    void it_cannot_handle_generate_if_em_stitching_not_enabled() {

        generateDocumentHandler =
            new GenerateDocumentHandler(
                true,
                false,
                documentGenerator,
                dateProvider,
                true);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = generateDocumentHandler.canHandle(callbackStage, callback);

                ImmutableSet<Event> eventsThatDontRequireStitching =
                    immutableEnumSet(
                        SUBMIT_APPEAL,
                        DRAFT_HEARING_REQUIREMENTS,
                        UPDATE_HEARING_REQUIREMENTS,
                        LIST_CASE,
                        GENERATE_DECISION_AND_REASONS,
                        GENERATE_HEARING_BUNDLE,
                        CUSTOMISE_HEARING_BUNDLE,
                        EDIT_CASE_LISTING,
                        SEND_DECISION_AND_REASONS,
                        ADJOURN_HEARING_WITHOUT_DATE,
                        END_APPEAL,
                        SUBMIT_CMA_REQUIREMENTS,
                        LIST_CMA,
                        END_APPEAL,
                        END_APPEAL_AUTOMATICALLY,
                        EDIT_APPEAL_AFTER_SUBMIT,
                        GENERATE_UPPER_TRIBUNAL_BUNDLE,
                        SUBMIT_REASONS_FOR_APPEAL,
                        SUBMIT_CLARIFYING_QUESTION_ANSWERS
                    );

                if (callbackStage.equals(PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
                    && (eventsThatDontRequireStitching.contains(event))) {
                    assertTrue(canHandle);
                } else if (event.equals(GENERATE_HEARING_BUNDLE)
                    || event.equals(CUSTOMISE_HEARING_BUNDLE)) {
                    assertFalse(canHandle);
                } else {
                    assertFalse(canHandle, "event: " + event + ", stage: " + callbackStage);
                }

            }

            reset(callback);
        }

    }

    @Test
    void should_not_allow_null_arguments() {

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
