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
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_OUT_OF_COUNTRY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATION_EDIT_LISTING_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATION_WITHDRAW_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DISABLE_OVERVIEW_PAGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_APPLICATION_DEADLINE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_DECISION_ALLOWED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_REMOTE_HEARING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RELIST_CASE_IMMEDIATELY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.GLASGOW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADA_SUITABILITY_REVIEW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ADJOURN_HEARING_WITHOUT_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.APPLY_FOR_FTPA_APPELLANT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.APPLY_FOR_FTPA_RESPONDENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.ASYNC_STITCHING_COMPLETE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.CHANGE_DIRECTION_DUE_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.CHANGE_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.CREATE_CASE_LINK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.CUSTOMISE_HEARING_BUNDLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.DECIDE_AN_APPLICATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.DRAFT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL_AFTER_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_CASE_LISTING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.END_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.END_APPEAL_AUTOMATICALLY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.GENERATE_DECISION_AND_REASONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.GENERATE_HEARING_BUNDLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.GENERATE_UPPER_TRIBUNAL_BUNDLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.LIST_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.LIST_CMA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MAINTAIN_CASE_LINKS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MANAGE_FEE_UPDATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MARK_APPEAL_AS_ADA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MARK_APPEAL_PAID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ADJOURNMENT_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_OUT_OF_TIME_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REINSTATE_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REMOVE_DETAINED_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_CASE_BUILDING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_HEARING_REQUIREMENTS_FEATURE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_RESPONDENT_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_RESPONDENT_REVIEW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_RESPONSE_AMEND;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.REQUEST_RESPONSE_REVIEW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RESIDENT_JUDGE_FTPA_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_DECISION_AND_REASONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_DIRECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_CLARIFYING_QUESTION_ANSWERS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_CMA_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_REASONS_FOR_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.TRANSFER_OUT_OF_ADA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_ADJUSTMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_TRIBUNAL_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPLOAD_ADDENDUM_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPLOAD_ADDITIONAL_EVIDENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
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
    private CaseDetails<AsylumCase> caseDetailsBefore;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private AsylumCase asylumCaseBefore;
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
        when(asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
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
            RECORD_ADJOURNMENT_DETAILS,
            REQUEST_CASE_BUILDING,
            ASYNC_STITCHING_COMPLETE
        ).forEach(event -> {

            AsylumCase expectedUpdatedCase = mock(AsylumCase.class);

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)).thenReturn(Optional.of(NO));

            if (event.equals(EDIT_CASE_LISTING)) {
                when(callback.getCaseDetails()).thenReturn(caseDetails);
                when(caseDetails.getCaseData()).thenReturn(asylumCase);
                when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)).thenReturn(Optional.empty());
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
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)).thenReturn(Optional.empty());
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

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(REMOVE_DETAINED_STATUS);
        assertThatThrownBy(() -> generateDocumentHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)).thenReturn(Optional.of(YES));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = generateDocumentHandler.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    &&
                    Arrays.asList(
                        SUBMIT_APPEAL,
                        SUBMIT_CASE,
                        DRAFT_HEARING_REQUIREMENTS,
                        UPDATE_HEARING_REQUIREMENTS,
                        UPDATE_HEARING_ADJUSTMENTS,
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
                        REQUEST_HEARING_REQUIREMENTS_FEATURE,
                        MARK_APPEAL_AS_ADA,
                        DECIDE_AN_APPLICATION,
                        APPLY_FOR_FTPA_RESPONDENT,
                        TRANSFER_OUT_OF_ADA,
                        RESIDENT_JUDGE_FTPA_DECISION,
                        APPLY_FOR_FTPA_APPELLANT,
                        MAINTAIN_CASE_LINKS,
                        UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER,
                        UPLOAD_ADDITIONAL_EVIDENCE,
                        CHANGE_HEARING_CENTRE,
                        CREATE_CASE_LINK,
                        REQUEST_RESPONSE_AMEND,
                        SEND_DIRECTION,
                        EDIT_APPEAL_AFTER_SUBMIT,
                        CHANGE_HEARING_CENTRE,
                        CHANGE_DIRECTION_DUE_DATE,
                        UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE,
                        UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE,
                        UPLOAD_ADDENDUM_EVIDENCE,
                        CHANGE_DIRECTION_DUE_DATE,
                        EDIT_APPEAL_AFTER_SUBMIT,
                        REINSTATE_APPEAL,
                        SUBMIT_CLARIFYING_QUESTION_ANSWERS,
                        UPDATE_TRIBUNAL_DECISION,
                        MANAGE_FEE_UPDATE
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
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)).thenReturn(Optional.of(YES));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = generateDocumentHandler.canHandle(callbackStage, callback);

                assertFalse(canHandle);
            }

            reset(callback);
        }
    }

    @Test
    void it_cannot_handle_if_edit_case_listing_for_remote_to_remote_hearing_channel_update() {

        when(asylumCaseBefore.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(GLASGOW));
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(GLASGOW));
        when(asylumCaseBefore.read(LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of("01/02/2024"));
        when(asylumCase.read(LIST_CASE_HEARING_DATE, String.class))
            .thenReturn(Optional.of("01/02/2024"));
        when(asylumCaseBefore.read(IS_REMOTE_HEARING, YesOrNo.class))
            .thenReturn(Optional.of(YES));
        when(asylumCase.read(IS_REMOTE_HEARING, YesOrNo.class)).thenReturn(Optional.of(YES));

        when(callback.getEvent()).thenReturn(EDIT_CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getCaseDetailsBefore()).thenReturn(Optional.of(caseDetailsBefore));
        when(caseDetailsBefore.getCaseData()).thenReturn(asylumCaseBefore);

        for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

            assertFalse(generateDocumentHandler.canHandle(callbackStage, callback));
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
            when(callback.getCaseDetails()).thenReturn(caseDetails);
            when(caseDetails.getCaseData()).thenReturn(asylumCase);
            when(asylumCase.read(RELIST_CASE_IMMEDIATELY, YesOrNo.class)).thenReturn(Optional.of(YES));

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = generateDocumentHandler.canHandle(callbackStage, callback);

                ImmutableSet<Event> eventsThatDontRequireStitching =
                    immutableEnumSet(
                        SUBMIT_APPEAL,
                        DRAFT_HEARING_REQUIREMENTS,
                        UPDATE_HEARING_REQUIREMENTS,
                        UPDATE_HEARING_ADJUSTMENTS,
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
                        REQUEST_HEARING_REQUIREMENTS_FEATURE,
                        MARK_APPEAL_AS_ADA,
                        DECIDE_AN_APPLICATION,
                        APPLY_FOR_FTPA_RESPONDENT,
                        TRANSFER_OUT_OF_ADA,
                        RESIDENT_JUDGE_FTPA_DECISION,
                        APPLY_FOR_FTPA_APPELLANT,
                        MAINTAIN_CASE_LINKS,
                        UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER,
                        UPLOAD_ADDITIONAL_EVIDENCE,
                        CHANGE_HEARING_CENTRE,
                        CREATE_CASE_LINK,
                        REQUEST_RESPONSE_AMEND,
                        SEND_DIRECTION,
                        CHANGE_HEARING_CENTRE,
                        CHANGE_DIRECTION_DUE_DATE,
                        UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE,
                        UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE,
                        UPLOAD_ADDENDUM_EVIDENCE,
                        CHANGE_DIRECTION_DUE_DATE,
                        REINSTATE_APPEAL,
                        SUBMIT_CLARIFYING_QUESTION_ANSWERS,
                        UPDATE_TRIBUNAL_DECISION,
                        MANAGE_FEE_UPDATE
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

        when(expectedUpdatedCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));

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
