package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isOnlyRemoteToRemoteHearingChannelUpdate;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.relistCaseImmediately;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Application;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DocumentGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DueDateService;

@Component
public class GenerateDocumentHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final boolean isDocmosisEnabled;
    private final boolean isEmStitchingEnabled;
    private final DocumentGenerator<AsylumCase> documentGenerator;
    private final DateProvider dateProvider;
    private final boolean isSaveAndContinueEnabled;
    private final DueDateService dueDateService;
    private final int ftpaAppealOutOfTimeWorkingDaysAdaAppeal;
    private final int ftpaAppealOutOfTimeDaysUk;
    private final int ftpaAppealOutOfTimeDaysOoc;
    private final int ftpaAppealOutOfTimeWorkingDaysInternalAdaCases;
    private final int ftpaAppealOutOfTimeDaysInternalNonAdaCases;

    public GenerateDocumentHandler(
            @Value("${featureFlag.docmosisEnabled}") boolean isDocmosisEnabled,
            @Value("${featureFlag.isEmStitchingEnabled}") boolean isEmStitchingEnabled,
            DocumentGenerator<AsylumCase> documentGenerator,
            DateProvider dateProvider,
            @Value("${featureFlag.isSaveAndContinueEnabled}") boolean isSaveAndContinueEnabled,
            DueDateService dueDateService,
            @Value("${ftpaAppealOutOfTimeDaysUk}") int ftpaAppealOutOfTimeDaysUk,
            @Value("${ftpaAppealOutOfTimeDaysOoc}") int ftpaAppealOutOfTimeDaysOoc,
            @Value("${ftpaAppealOutOfTimeWorkingDaysAdaAppeal}") int ftpaAppealOutOfTimeWorkingDaysAdaAppeal,
            @Value("${ftpaAppealOutOfTimeWorkingDaysInternalAdaCases}") int ftpaAppealOutOfTimeWorkingDaysInternalAdaCases,
            @Value("${ftpaAppealOutOfTimeDaysInternalNonAdaCases}") int ftpaAppealOutOfTimeDaysInternalNonAdaCases) {
        this.isDocmosisEnabled = isDocmosisEnabled;
        this.isEmStitchingEnabled = isEmStitchingEnabled;
        this.documentGenerator = documentGenerator;
        this.dateProvider = dateProvider;
        this.isSaveAndContinueEnabled = isSaveAndContinueEnabled;
        this.dueDateService = dueDateService;
        this.ftpaAppealOutOfTimeWorkingDaysAdaAppeal = ftpaAppealOutOfTimeWorkingDaysAdaAppeal;
        this.ftpaAppealOutOfTimeDaysUk = ftpaAppealOutOfTimeDaysUk;
        this.ftpaAppealOutOfTimeDaysOoc = ftpaAppealOutOfTimeDaysOoc;
        this.ftpaAppealOutOfTimeWorkingDaysInternalAdaCases = ftpaAppealOutOfTimeWorkingDaysInternalAdaCases;
        this.ftpaAppealOutOfTimeDaysInternalNonAdaCases = ftpaAppealOutOfTimeDaysInternalNonAdaCases;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        List<Event> allowedEvents = Lists.newArrayList(
            Event.SUBMIT_APPEAL,
            Event.DRAFT_HEARING_REQUIREMENTS,
            Event.UPDATE_HEARING_REQUIREMENTS,
            Event.LIST_CASE,
            Event.GENERATE_HEARING_BUNDLE,
            Event.CUSTOMISE_HEARING_BUNDLE,
            Event.GENERATE_UPDATED_HEARING_BUNDLE,
            Event.GENERATE_DECISION_AND_REASONS,
            Event.SEND_DECISION_AND_REASONS,
            Event.ADJOURN_HEARING_WITHOUT_DATE,
            Event.END_APPEAL,
            Event.SUBMIT_CMA_REQUIREMENTS,
            Event.LIST_CMA,
            Event.END_APPEAL,
            Event.END_APPEAL_AUTOMATICALLY,
            Event.EDIT_APPEAL_AFTER_SUBMIT,
            Event.SUBMIT_REASONS_FOR_APPEAL,
            Event.SUBMIT_CLARIFYING_QUESTION_ANSWERS,
            Event.ADA_SUITABILITY_REVIEW,
            Event.REQUEST_CASE_BUILDING,
            Event.REQUEST_RESPONDENT_REVIEW,
            Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE,
            Event.ASYNC_STITCHING_COMPLETE,
            Event.RECORD_OUT_OF_TIME_DECISION,
            Event.REQUEST_RESPONDENT_EVIDENCE,
            Event.RECORD_REMISSION_DECISION,
            Event.MARK_APPEAL_PAID,
            Event.REQUEST_RESPONSE_REVIEW,
            Event.REQUEST_HEARING_REQUIREMENTS_FEATURE,
            Event.MARK_APPEAL_AS_ADA,
            Event.DECIDE_AN_APPLICATION,
            Event.APPLY_FOR_FTPA_RESPONDENT,
            Event.TRANSFER_OUT_OF_ADA,
            Event.RESIDENT_JUDGE_FTPA_DECISION,
            Event.APPLY_FOR_FTPA_APPELLANT,
            Event.MAINTAIN_CASE_LINKS,
            Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER,
            Event.UPLOAD_ADDITIONAL_EVIDENCE,
            Event.CHANGE_HEARING_CENTRE,
            Event.CREATE_CASE_LINK,
            Event.REQUEST_RESPONSE_AMEND,
            Event.SEND_DIRECTION,
            Event.CHANGE_DIRECTION_DUE_DATE,
            Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE,
            Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE,
            Event.UPLOAD_ADDENDUM_EVIDENCE,
            Event.UPDATE_HEARING_ADJUSTMENTS,
            Event.REINSTATE_APPEAL,
            Event.GENERATE_UPPER_TRIBUNAL_BUNDLE,
            Event.MANAGE_FEE_UPDATE,
            Event.UPDATE_TRIBUNAL_DECISION);
        if (isEmStitchingEnabled) {
            allowedEvents.add(Event.SUBMIT_CASE);
            if (!isSaveAndContinueEnabled) {
                allowedEvents.add(Event.BUILD_CASE);
            }
        }
        if (!relistCaseImmediately(asylumCase, false)) {
            allowedEvents.add(Event.RECORD_ADJOURNMENT_DETAILS);
        }
        if (generateDocumentsForEditCaseListingEvent(callback)) {
            allowedEvents.add(Event.EDIT_CASE_LISTING);
        }

        return isDocmosisEnabled
               && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && allowedEvents.contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCaseWithGeneratedDocument = documentGenerator.generate(callback);

        if (Event.EDIT_CASE_LISTING.equals(callback.getEvent())) {
            removeFlagsForRecordedApplication(
                asylumCaseWithGeneratedDocument,
                callback.getCaseDetails().getState()
            );
            changeEditListingApplicationsToCompleted(asylumCaseWithGeneratedDocument);
        }

        if (Event.SEND_DECISION_AND_REASONS.equals(callback.getEvent())) {
            saveDecisionDetails(asylumCaseWithGeneratedDocument);
        }

        return new PreSubmitCallbackResponse<>(asylumCaseWithGeneratedDocument);
    }

    private void saveDecisionDetails(AsylumCase asylumCase) {
        asylumCase.write(
            APPEAL_DECISION,
            StringUtils.capitalize(
                asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class)
                    .orElseThrow(() -> new IllegalStateException("decision property must be set"))
                    .getValue()
            )
        );

        LocalDate appealDate = dateProvider.now();
        asylumCase.write(APPEAL_DATE, appealDate.toString());
        asylumCase.write(APPEAL_DECISION_AVAILABLE, YES);
        asylumCase.write(FTPA_APPLICATION_DEADLINE, getFtpaApplicationDeadline(asylumCase, appealDate));
        asylumCase.clear(UPDATED_APPEAL_DECISION);
    }

    private void changeEditListingApplicationsToCompleted(AsylumCase asylumCase) {
        asylumCase.write(APPLICATIONS, asylumCase.<List<IdValue<Application>>>read(APPLICATIONS)
            .orElse(emptyList())
            .stream()
            .map(application -> {
                String applicationType = application.getValue().getApplicationType();
                if (ApplicationType.ADJOURN.toString().equals(applicationType)
                    || ApplicationType.EXPEDITE.toString().equals(applicationType)
                    || ApplicationType.TRANSFER.toString().equals(applicationType)) {

                    return new IdValue<>(application.getId(), new Application(
                        application.getValue().getApplicationDocuments(),
                        application.getValue().getApplicationSupplier(),
                        applicationType,
                        application.getValue().getApplicationReason(),
                        application.getValue().getApplicationDate(),
                        application.getValue().getApplicationDecision(),
                        application.getValue().getApplicationDecisionReason(),
                        application.getValue().getApplicationDateOfDecision(),
                        "Completed"
                    ));
                }

                return application;
            })
            .collect(Collectors.toList())
        );
    }

    private void removeFlagsForRecordedApplication(AsylumCase asylumCase, State currentState) {

        boolean isApplicationRecorded = asylumCase.read(APPLICATION_EDIT_LISTING_EXISTS, String.class)
            .map(flag -> flag.equalsIgnoreCase("Yes"))
            .orElse(false);

        if (isApplicationRecorded) {
            asylumCase.clear(APPLICATION_EDIT_LISTING_EXISTS);

            boolean isWithdrawExists = asylumCase.read(APPLICATION_WITHDRAW_EXISTS, String.class)
                .map(flag -> flag.equalsIgnoreCase("Yes"))
                .orElse(false);

            if (!isWithdrawExists) {
                asylumCase.clear(DISABLE_OVERVIEW_PAGE);
                asylumCase.write(CURRENT_CASE_STATE_VISIBLE_TO_CASE_OFFICER, currentState);
            }
        }
    }

    private String getFtpaApplicationDeadline(AsylumCase asylumCase, LocalDate appealDate) {
        boolean isInternalCase = isInternalCase(asylumCase);
        boolean isAda = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class).orElse(YesOrNo.NO).equals(YES);

        if (isInternalCase) {
            return getFtpaApplicationDeadlineForInternalCase(appealDate, isAda).toString();
        }

        LocalDate ftpaApplicationDeadline;

        // APPEAL_OUT_OF_COUNTRY used instead of previous OUT_OF_COUNTRY_DECISION_TYPE because in AiP UI screen with OUT_OF_COUNTRY_DECISION_TYPE is not implemented yet
        boolean isOoc = asylumCase.read(APPEAL_OUT_OF_COUNTRY, YesOrNo.class).map(ooc -> YES == ooc).orElse(false);

        if (isAda) {
            ftpaApplicationDeadline = dueDateService.calculateDueDate(appealDate.atStartOfDay(ZoneOffset.UTC), ftpaAppealOutOfTimeWorkingDaysAdaAppeal).toLocalDate();
        } else if (isOoc) {
            ftpaApplicationDeadline = appealDate.plusDays(ftpaAppealOutOfTimeDaysOoc);
        } else {
            ftpaApplicationDeadline = appealDate.plusDays(ftpaAppealOutOfTimeDaysUk);
        }

        return ftpaApplicationDeadline.toString();
    }

    private LocalDate getFtpaApplicationDeadlineForInternalCase(LocalDate appealDate, boolean isAda) {
        return isAda ? dueDateService.calculateDueDate(appealDate.atStartOfDay(ZoneOffset.UTC),
                        ftpaAppealOutOfTimeWorkingDaysInternalAdaCases).toLocalDate() :
                appealDate.plusDays(ftpaAppealOutOfTimeDaysInternalNonAdaCases);
    }

    private boolean generateDocumentsForEditCaseListingEvent(Callback<AsylumCase> callback) {
        // Documents are not generated if the update is remote to remote hearing channel update
        // (VID to TEL or TEL to VID)
        return !isOnlyRemoteToRemoteHearingChannelUpdate(callback);
    }
}

