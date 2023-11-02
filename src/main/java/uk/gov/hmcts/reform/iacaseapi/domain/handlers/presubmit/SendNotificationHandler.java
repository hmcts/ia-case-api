package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PAYMENT_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.NotificationSender;

@Component
public class SendNotificationHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final NotificationSender<AsylumCase> notificationSender;
    @Value("${featureFlag.isSaveAndContinueEnabled}")
    private boolean isSaveAndContinueEnabled;
    private final FeatureToggler featureToggler;

    public SendNotificationHandler(
        NotificationSender<AsylumCase> notificationSender,
        FeatureToggler featureToggler
    ) {
        this.notificationSender = notificationSender;
        this.featureToggler = featureToggler;
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

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (isNotificationTurnedOff(asylumCase)) {
            return false;
        }

        if (isInternalCase(asylumCase)) {
            return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                    && getInternalEventsToHandle(callback).contains(callback.getEvent());
        }

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && getEventsToHandle(callback).contains(callback.getEvent());

    }

    private List<Event> getEventsToHandle(Callback<AsylumCase> callback) {
        List<Event> eventsToHandle = Lists.newArrayList(
            Event.SUBMIT_APPEAL,
            Event.SEND_DIRECTION,
            Event.CHANGE_DIRECTION_DUE_DATE,
            Event.REQUEST_RESPONDENT_EVIDENCE,
            Event.UPLOAD_RESPONDENT_EVIDENCE,
            Event.REQUEST_RESPONDENT_REVIEW,
            Event.ADD_APPEAL_RESPONSE,
            Event.REQUEST_HEARING_REQUIREMENTS,
            Event.DRAFT_HEARING_REQUIREMENTS,
            Event.REVIEW_HEARING_REQUIREMENTS,
            Event.REQUEST_HEARING_REQUIREMENTS_FEATURE,
            Event.DECISION_WITHOUT_HEARING,
            Event.LIST_CASE,
            Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS,
            Event.EDIT_CASE_LISTING,
            Event.END_APPEAL,
            Event.UPLOAD_HOME_OFFICE_BUNDLE,
            Event.REQUEST_CASE_BUILDING,
            Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE,
            Event.SEND_DECISION_AND_REASONS,
            Event.UPLOAD_ADDITIONAL_EVIDENCE,
            Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE,
            Event.UPLOAD_ADDENDUM_EVIDENCE,
            Event.UPLOAD_ADDENDUM_EVIDENCE_LEGAL_REP,
            Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE,
            Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER,
            Event.REQUEST_REASONS_FOR_APPEAL,
            Event.SUBMIT_REASONS_FOR_APPEAL,
            Event.UPDATE_HEARING_ADJUSTMENTS,
            Event.REMOVE_APPEAL_FROM_ONLINE,
            Event.CHANGE_HEARING_CENTRE,
            Event.APPLY_FOR_FTPA_APPELLANT,
            Event.APPLY_FOR_FTPA_RESPONDENT,
            Event.REVIEW_TIME_EXTENSION,
            Event.SUBMIT_TIME_EXTENSION,
            Event.SEND_DIRECTION_WITH_QUESTIONS,
            Event.SUBMIT_CLARIFYING_QUESTION_ANSWERS,
            Event.REQUEST_CASE_EDIT,
            Event.FORCE_CASE_TO_CASE_UNDER_REVIEW,
            Event.FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS,
            Event.SUBMIT_TIME_EXTENSION,
            Event.ADJOURN_HEARING_WITHOUT_DATE,
            Event.RESTORE_STATE_FROM_ADJOURN,
            Event.REQUEST_CMA_REQUIREMENTS,
            Event.SUBMIT_CMA_REQUIREMENTS,
            Event.SUBMIT_CASE,
            Event.EDIT_APPEAL_AFTER_SUBMIT,
            Event.LINK_APPEAL,
            Event.UNLINK_APPEAL,
            Event.EDIT_DOCUMENTS,
            Event.LIST_CMA,
            Event.FORCE_REQUEST_CASE_BUILDING,
            Event.LEADERSHIP_JUDGE_FTPA_DECISION,
            Event.REQUEST_RESPONSE_AMEND,
            Event.RESIDENT_JUDGE_FTPA_DECISION,
            Event.MARK_APPEAL_PAID,
            Event.MAKE_AN_APPLICATION,
            Event.REINSTATE_APPEAL,
            Event.DECIDE_AN_APPLICATION,
            Event.REQUEST_NEW_HEARING_REQUIREMENTS,
            Event.RECORD_REMISSION_DECISION,
            Event.REQUEST_FEE_REMISSION,
            Event.MANAGE_FEE_UPDATE,
            Event.REQUEST_FEE_REMISSION,
            Event.RECORD_OUT_OF_TIME_DECISION,
            Event.END_APPEAL_AUTOMATICALLY,
            Event.UPDATE_PAYMENT_STATUS,
            Event.ADA_SUITABILITY_REVIEW,
            Event.TRANSFER_OUT_OF_ADA,
            Event.MARK_APPEAL_AS_ADA,
            Event.UPDATE_PAYMENT_STATUS,
            Event.REMOVE_DETAINED_STATUS,
            Event.MARK_APPEAL_AS_DETAINED,
            Event.CREATE_CASE_LINK,
            Event.MAINTAIN_CASE_LINKS,
            Event.UPDATE_PAYMENT_STATUS,
            Event.MARK_AS_READY_FOR_UT_TRANSFER,
            Event.UPDATE_DETENTION_LOCATION
        );
        if (!isSaveAndContinueEnabled) {
            eventsToHandle.add(Event.BUILD_CASE);
        }
        if (!isPaid(callback)) {
            eventsToHandle.add(Event.END_APPEAL_AUTOMATICALLY);
        }
        if (isAipJourney(callback.getCaseDetails().getCaseData()) && isPaid(callback)) {
            eventsToHandle.add(Event.PAYMENT_APPEAL);
        }

        if (isAipJourney(callback.getCaseDetails().getCaseData())
            && !featureToggler.getValue("aip-ftpa-feature", false)) {

            eventsToHandle.remove(Event.APPLY_FOR_FTPA_RESPONDENT);
            eventsToHandle.remove(Event.APPLY_FOR_FTPA_APPELLANT);
        }
        if (!isExAdaCaseWithHearingRequirementsSubmitted(callback)) {
            eventsToHandle.add(Event.REQUEST_RESPONSE_REVIEW);
        }
        return eventsToHandle;
    }

    private Set<Event> getInternalEventsToHandle(Callback<AsylumCase> callback) {
        Set<Event> eventsToHandle = Sets.newHashSet(
                Event.EDIT_APPEAL_AFTER_SUBMIT,
                Event.REQUEST_RESPONDENT_EVIDENCE,
                Event.REQUEST_RESPONDENT_REVIEW,
                Event.DECIDE_AN_APPLICATION,
                Event.MAKE_AN_APPLICATION,
                Event.ADA_SUITABILITY_REVIEW,
                Event.APPLY_FOR_FTPA_APPELLANT,
                Event.APPLY_FOR_FTPA_RESPONDENT,
                Event.REMOVE_DETAINED_STATUS,
                Event.REINSTATE_APPEAL,
                Event.RECORD_OUT_OF_TIME_DECISION,
                Event.END_APPEAL,
                Event.SUBMIT_APPEAL,
                Event.UPDATE_HEARING_ADJUSTMENTS,
                Event.MARK_AS_READY_FOR_UT_TRANSFER,
                Event.REQUEST_CASE_BUILDING,
                Event.UPDATE_DETENTION_LOCATION,
                Event.GENERATE_HEARING_BUNDLE,
                Event.SEND_DECISION_AND_REASONS,
                Event.END_APPEAL_AUTOMATICALLY,
                Event.RECORD_REMISSION_DECISION,
                Event.MARK_APPEAL_PAID,
                Event.LIST_CASE,
                Event.REQUEST_HEARING_REQUIREMENTS_FEATURE,
                Event.REQUEST_RESPONSE_REVIEW,
                Event.MARK_APPEAL_AS_ADA,
                Event.EDIT_CASE_LISTING,
                Event.TRANSFER_OUT_OF_ADA,
                Event.SEND_DIRECTION,
                Event.RESIDENT_JUDGE_FTPA_DECISION,
                Event.MAINTAIN_CASE_LINKS,
                Event.CHANGE_HEARING_CENTRE,
                Event.CREATE_CASE_LINK,
                Event.UPLOAD_ADDITIONAL_EVIDENCE,
                Event.REQUEST_RESPONSE_AMEND,
                Event.UPLOAD_ADDENDUM_EVIDENCE_ADMIN_OFFICER,
                Event.EDIT_APPEAL_AFTER_SUBMIT,
                Event.CHANGE_HEARING_CENTRE,
                Event.CHANGE_DIRECTION_DUE_DATE,
                Event.UPLOAD_ADDITIONAL_EVIDENCE_HOME_OFFICE,
                Event.UPLOAD_ADDENDUM_EVIDENCE_HOME_OFFICE,
                Event.UPLOAD_ADDENDUM_EVIDENCE
        );

        if (!isSaveAndContinueEnabled) {
            //eventsToHandle.add(Event.BUILD_CASE);
        }
        if (!isExAdaCaseWithHearingRequirementsSubmitted(callback)) {
            eventsToHandle.add(Event.UPLOAD_HOME_OFFICE_APPEAL_RESPONSE);
        }

        // @TODO Get rid of this condition when appellant application notification is implemented
        if (isRespondentApplication(callback.getCaseDetails().getCaseData())) {
            eventsToHandle.add(Event.RESIDENT_JUDGE_FTPA_DECISION);
            eventsToHandle.add(Event.LEADERSHIP_JUDGE_FTPA_DECISION);
        }

        return eventsToHandle;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCaseWithNotificationMarker = notificationSender.send(callback);

        return new PreSubmitCallbackResponse<>(asylumCaseWithNotificationMarker);
    }

    private boolean isPaid(Callback<AsylumCase> callback) {
        return callback.getCaseDetails().getCaseData()
            .read(PAYMENT_STATUS, PaymentStatus.class)
            .map(paymentStatus -> paymentStatus.equals(PaymentStatus.PAID))
            .orElse(false);
    }

    private boolean isExAdaCaseWithHearingRequirementsSubmitted(Callback<AsylumCase> callback) {
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        return asylumCase
                   .read(ADA_HEARING_REQUIREMENTS_SUBMITTED, YesOrNo.class)
                   .orElse(NO)
                   .equals(YES)
               && asylumCase.read(HAS_TRANSFERRED_OUT_OF_ADA, YesOrNo.class)
                   .orElse(NO)
                   .equals(YES);
    }

    private boolean isRespondentApplication(AsylumCase asylumCase) {
        return asylumCase.read(FTPA_APPLICANT_TYPE, String.class)
            .map("respondent"::equals).orElse(false);
    }
}
