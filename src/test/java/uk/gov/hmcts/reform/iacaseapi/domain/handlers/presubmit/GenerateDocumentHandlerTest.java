package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.immutableEnumSet;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;

import com.google.common.collect.ImmutableSet;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DueDateService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class GenerateDocumentHandlerTest {

    private static final int ftpaAppealOutOfTimeWorkingDaysAdaAppeal = 5;
    private static final int ftpaAppealOutOfTimeDaysOoc = 28;
    private static final int ftpaAppealOutOfTimeDaysUk = 14;

    @Mock
    private DocumentGenerator<AsylumCase> documentGenerator;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private DateProvider dateProvider;
    @Mock
    private DueDateService dueDateService;
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
                    true,
                dueDateService,
                ftpaAppealOutOfTimeDaysUk,
                ftpaAppealOutOfTimeDaysOoc,
                ftpaAppealOutOfTimeWorkingDaysAdaAppeal
                    );
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
                String expectedFtpaDeadlineUK = now.plusDays(5).toString();
                final ZonedDateTime zonedDateTime = now.atStartOfDay(ZoneOffset.UTC);
                final ZonedDateTime zonedDueDateTime = LocalDate.parse(expectedFtpaDeadlineUK).atStartOfDay(ZoneOffset.UTC);

                when(expectedUpdatedCase.read(IS_DECISION_ALLOWED, AppealDecision.class))
                    .thenReturn(Optional.of(AppealDecision.ALLOWED));
                when(dateProvider.now()).thenReturn(now);
                when(dueDateService.calculateDueDate(zonedDateTime, ftpaAppealOutOfTimeDaysUk)).thenReturn(zonedDueDateTime);
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
                        ADA_SUITABILITY_REVIEW,
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
                true,
                    dueDateService,
                    ftpaAppealOutOfTimeDaysUk,
                    ftpaAppealOutOfTimeDaysOoc,
                    ftpaAppealOutOfTimeWorkingDaysAdaAppeal);

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
                true,
                    dueDateService,
                    ftpaAppealOutOfTimeDaysUk,
                    ftpaAppealOutOfTimeDaysOoc,
                    ftpaAppealOutOfTimeWorkingDaysAdaAppeal);

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = generateDocumentHandler.canHandle(callbackStage, callback);

                ImmutableSet<Event> eventsThatDontRequireStitching =
                    immutableEnumSet(
                        SUBMIT_APPEAL,
                        DRAFT_HEARING_REQUIREMENTS,
                        UPDATE_HEARING_REQUIREMENTS,
                        ADA_SUITABILITY_REVIEW,
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

    @Test
    void should_send_decision_and_reasons_and_populate_ftpa_field_ada_appeal() {
        String expectedFtpaDeadlineAda = now.plusDays(ftpaAppealOutOfTimeWorkingDaysAdaAppeal).toString();
        final ZonedDateTime zonedDateTime = now.atStartOfDay(ZoneOffset.UTC);
        final ZonedDateTime zonedDueDateTime = LocalDate.parse(expectedFtpaDeadlineAda).atStartOfDay(ZoneOffset.UTC);

        AsylumCase expectedUpdatedCase = mock(AsylumCase.class);
        when(callback.getEvent()).thenReturn(SEND_DECISION_AND_REASONS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(state);
        when(expectedUpdatedCase.read(IS_DECISION_ALLOWED, AppealDecision.class))
                .thenReturn(Optional.of(AppealDecision.ALLOWED));
        when(dateProvider.now()).thenReturn(now);

        when(expectedUpdatedCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));
        when(dueDateService.calculateDueDate(zonedDateTime, ftpaAppealOutOfTimeWorkingDaysAdaAppeal)).thenReturn(zonedDueDateTime);

        when(documentGenerator.generate(callback)).thenReturn(expectedUpdatedCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(expectedUpdatedCase, callbackResponse.getData());
        verify(documentGenerator, times(1)).generate(callback);

        verify(expectedUpdatedCase).write(FTPA_APPLICATION_DEADLINE, zonedDueDateTime.toLocalDate().toString());
    }

    @Test
    void should_send_decision_and_reasons_and_populate_ftpa_field_ooc_appeal() {
        String expectedFtpaDeadlineOoc = now.plusDays(ftpaAppealOutOfTimeDaysOoc).toString();
        final ZonedDateTime zonedDateTime = now.atStartOfDay(ZoneOffset.UTC);
        final ZonedDateTime zonedDueDateTime = LocalDate.parse(expectedFtpaDeadlineOoc).atStartOfDay(ZoneOffset.UTC);

        AsylumCase expectedUpdatedCase = mock(AsylumCase.class);
        when(callback.getEvent()).thenReturn(SEND_DECISION_AND_REASONS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(state);
        when(expectedUpdatedCase.read(IS_DECISION_ALLOWED, AppealDecision.class))
                .thenReturn(Optional.of(AppealDecision.ALLOWED));
        when(dateProvider.now()).thenReturn(now);

        when(expectedUpdatedCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
        when(expectedUpdatedCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
                .thenReturn(Optional.of(OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS));
        when(dueDateService.calculateDueDate(zonedDateTime, ftpaAppealOutOfTimeDaysOoc)).thenReturn(zonedDueDateTime);

        when(documentGenerator.generate(callback)).thenReturn(expectedUpdatedCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(expectedUpdatedCase, callbackResponse.getData());
        verify(documentGenerator, times(1)).generate(callback);

        verify(expectedUpdatedCase).write(FTPA_APPLICATION_DEADLINE, zonedDueDateTime.toLocalDate().toString());
    }

    @Test
    void should_send_decision_and_reasons_and_populate_ftpa_field_uk_appeal() {
        String expectedFtpaDeadlineUk = now.plusDays(ftpaAppealOutOfTimeDaysUk).toString();
        final ZonedDateTime zonedDateTime = now.atStartOfDay(ZoneOffset.UTC);
        final ZonedDateTime zonedDueDateTime = LocalDate.parse(expectedFtpaDeadlineUk).atStartOfDay(ZoneOffset.UTC);

        AsylumCase expectedUpdatedCase = mock(AsylumCase.class);
        when(callback.getEvent()).thenReturn(SEND_DECISION_AND_REASONS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(state);
        when(expectedUpdatedCase.read(IS_DECISION_ALLOWED, AppealDecision.class))
                .thenReturn(Optional.of(AppealDecision.ALLOWED));
        when(dateProvider.now()).thenReturn(now);

        when(expectedUpdatedCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
        when(expectedUpdatedCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
                .thenReturn(Optional.empty());
        when(dueDateService.calculateDueDate(zonedDateTime, ftpaAppealOutOfTimeDaysUk)).thenReturn(zonedDueDateTime);

        when(documentGenerator.generate(callback)).thenReturn(expectedUpdatedCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(expectedUpdatedCase, callbackResponse.getData());
        verify(documentGenerator, times(1)).generate(callback);

        verify(expectedUpdatedCase).write(FTPA_APPLICATION_DEADLINE, zonedDueDateTime.toLocalDate().toString());
    }

}
