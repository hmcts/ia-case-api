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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
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

    private static final int FTPA_DUE_IN_WORKING_DAYS_ADA = 5;
    private static final int FTPA_DUE_IN_DAYS_OOC = 28;
    private static final int FTPA_DUE_IN_DAYS_UK = 14;
    private static final int FTPA_DUE_IN_WORKING_DAYS_ADA_INTERNAL = 7;
    private static final int FTPA_DUE_IN_DAYS_NON_ADA_INTERNAL = 14;
    private static final LocalDate FAKE_APPEAL_DATE = LocalDate.parse("2023-02-11");
    private static final LocalDate EXPECTED_FTPA_DEADLINE_ADA = LocalDate.parse("2023-02-21");
    private static final LocalDate EXPECTED_FTPA_DEADLINE_UK = FAKE_APPEAL_DATE.plusDays(FTPA_DUE_IN_DAYS_UK);
    private static final LocalDate EXPECTED_FTPA_DEADLINE_OOC = FAKE_APPEAL_DATE.plusDays(FTPA_DUE_IN_DAYS_OOC);
    @Mock
    private DocumentGenerator<AsylumCase> documentGenerator;
    @Mock
    private Callback<AsylumCase> callback;
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
                FTPA_DUE_IN_DAYS_UK,
                FTPA_DUE_IN_DAYS_OOC,
                FTPA_DUE_IN_WORKING_DAYS_ADA,
                FTPA_DUE_IN_WORKING_DAYS_ADA_INTERNAL,
                FTPA_DUE_IN_DAYS_NON_ADA_INTERNAL
            );
    }

    void setUpSendDecisionsAndReasonsData(AsylumCase asylumCase) {
        when(callback.getEvent()).thenReturn(SEND_DECISION_AND_REASONS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getState()).thenReturn(state);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class))
                .thenReturn(Optional.of(AppealDecision.ALLOWED));

        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
                .thenReturn(Optional.empty());
        when(dateProvider.now()).thenReturn(FAKE_APPEAL_DATE);
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
            SUBMIT_CLARIFYING_QUESTION_ANSWERS,
            REQUEST_CASE_BUILDING,
            ASYNC_STITCHING_COMPLETE
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
                when(dateProvider.now()).thenReturn(FAKE_APPEAL_DATE);
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
                verify(expectedUpdatedCase).write(APPEAL_DATE, FAKE_APPEAL_DATE.toString());
                verify(expectedUpdatedCase).write(FTPA_APPLICATION_DEADLINE,EXPECTED_FTPA_DEADLINE_UK.toString());
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
                        SUBMIT_CLARIFYING_QUESTION_ANSWERS,
                        REQUEST_CASE_BUILDING,
                        REQUEST_RESPONDENT_REVIEW,
                        UPLOAD_HOME_OFFICE_APPEAL_RESPONSE,
                        ASYNC_STITCHING_COMPLETE,
                        RECORD_OUT_OF_TIME_DECISION,
                        REQUEST_RESPONDENT_EVIDENCE,
                        RECORD_REMISSION_DECISION,
                        MARK_APPEAL_PAID,
                        REQUEST_RESPONSE_REVIEW,
                        REQUEST_HEARING_REQUIREMENTS_FEATURE
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
                    FTPA_DUE_IN_DAYS_UK,
                    FTPA_DUE_IN_DAYS_OOC,
                    FTPA_DUE_IN_WORKING_DAYS_ADA,
                    FTPA_DUE_IN_WORKING_DAYS_ADA_INTERNAL,
                    FTPA_DUE_IN_DAYS_NON_ADA_INTERNAL
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
    void it_cannot_handle_generate_if_em_stitching_not_enabled() {

        generateDocumentHandler =
            new GenerateDocumentHandler(
                true,
                false,
                documentGenerator,
                dateProvider,
                true,
                dueDateService,
                FTPA_DUE_IN_DAYS_UK,
                FTPA_DUE_IN_DAYS_OOC,
                FTPA_DUE_IN_WORKING_DAYS_ADA,
                FTPA_DUE_IN_WORKING_DAYS_ADA_INTERNAL,
                FTPA_DUE_IN_DAYS_NON_ADA_INTERNAL
            );

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
                        SUBMIT_CLARIFYING_QUESTION_ANSWERS,
                        REQUEST_CASE_BUILDING,
                        REQUEST_RESPONDENT_REVIEW,
                        UPLOAD_HOME_OFFICE_APPEAL_RESPONSE,
                        ASYNC_STITCHING_COMPLETE,
                        RECORD_OUT_OF_TIME_DECISION,
                        REQUEST_RESPONDENT_EVIDENCE,
                        RECORD_REMISSION_DECISION,
                        MARK_APPEAL_PAID,
                        REQUEST_RESPONSE_REVIEW,
                        REQUEST_HEARING_REQUIREMENTS_FEATURE
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
        AsylumCase expectedUpdatedCase = mock(AsylumCase.class);
        setUpSendDecisionsAndReasonsData(expectedUpdatedCase);

        when(expectedUpdatedCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));

        final ZonedDateTime fakeAppealDateTime = FAKE_APPEAL_DATE.atStartOfDay(ZoneOffset.UTC);
        when(dueDateService.calculateDueDate(fakeAppealDateTime, FTPA_DUE_IN_WORKING_DAYS_ADA))
                .thenReturn(ZonedDateTime.of(EXPECTED_FTPA_DEADLINE_ADA.atStartOfDay(), ZoneOffset.UTC));

        when(documentGenerator.generate(callback)).thenReturn(expectedUpdatedCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(expectedUpdatedCase, callbackResponse.getData());
        verify(documentGenerator, times(1)).generate(callback);

        verify(expectedUpdatedCase).write(FTPA_APPLICATION_DEADLINE, EXPECTED_FTPA_DEADLINE_ADA.toString());
    }

    @Test
    void should_send_decision_and_reasons_and_populate_ftpa_field_ooc_appeal() {
        AsylumCase expectedUpdatedCase = mock(AsylumCase.class);
        setUpSendDecisionsAndReasonsData(expectedUpdatedCase);

        when(expectedUpdatedCase.read(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class))
                .thenReturn(Optional.of(OutOfCountryDecisionType.REFUSAL_OF_HUMAN_RIGHTS));

        when(documentGenerator.generate(callback)).thenReturn(expectedUpdatedCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(expectedUpdatedCase, callbackResponse.getData());
        verify(documentGenerator, times(1)).generate(callback);

        verify(expectedUpdatedCase).write(FTPA_APPLICATION_DEADLINE, EXPECTED_FTPA_DEADLINE_OOC.toString());
    }

    @Test
    void should_send_decision_and_reasons_and_populate_ftpa_field_uk_appeal() {
        AsylumCase expectedUpdatedCase = mock(AsylumCase.class);
        setUpSendDecisionsAndReasonsData(expectedUpdatedCase);

        when(documentGenerator.generate(callback)).thenReturn(expectedUpdatedCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(expectedUpdatedCase, callbackResponse.getData());
        verify(documentGenerator, times(1)).generate(callback);

        verify(expectedUpdatedCase).write(FTPA_APPLICATION_DEADLINE, EXPECTED_FTPA_DEADLINE_UK.toString());
    }

    @ParameterizedTest
    @EnumSource(YesOrNo.class)
    void should_populate_ftpa_deadline_for_internal_case(YesOrNo yesOrNo) {
        AsylumCase expectedUpdatedCase = mock(AsylumCase.class);
        setUpSendDecisionsAndReasonsData(expectedUpdatedCase);

        when(expectedUpdatedCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class))
                .thenReturn(Optional.of(yesOrNo));
        when(expectedUpdatedCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        final ZonedDateTime fakeAppealDateTime = FAKE_APPEAL_DATE.atStartOfDay(ZoneOffset.UTC);
        when(dueDateService.calculateDueDate(fakeAppealDateTime, FTPA_DUE_IN_WORKING_DAYS_ADA_INTERNAL))
                .thenReturn(ZonedDateTime.of(EXPECTED_FTPA_DEADLINE_ADA.atStartOfDay(), ZoneOffset.UTC));

        when(documentGenerator.generate(callback)).thenReturn(expectedUpdatedCase);

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(expectedUpdatedCase, callbackResponse.getData());
        verify(documentGenerator, times(1)).generate(callback);

        if (yesOrNo.equals(YesOrNo.YES)) {
            verify(expectedUpdatedCase).write(FTPA_APPLICATION_DEADLINE, EXPECTED_FTPA_DEADLINE_ADA.toString());
        } else {
            verify(expectedUpdatedCase).write(FTPA_APPLICATION_DEADLINE, EXPECTED_FTPA_DEADLINE_UK.toString());
        }
    }

}
