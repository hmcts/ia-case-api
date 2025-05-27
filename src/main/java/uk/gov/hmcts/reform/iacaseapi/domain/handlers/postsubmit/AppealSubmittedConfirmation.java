package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.DC;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.HELP_WITH_FEES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.HO_WAIVER_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.NO_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isEjpCase;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAppellantInDetention;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfTimeDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdSupplementaryUpdater;

@Slf4j
@Component
public class AppealSubmittedConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private static final String PAYMENT_OPTION_PAY_OFFLINE = "payOffline";
    private static final String PAYMENT_OPTION_PAY_LATER = "payLater";
    private static final String WHAT_HAPPENS_NEXT_LABEL = "#### What happens next\n\n";
    private static final String DO_THIS_NEXT_LABEL = "#### Do this next\n\n";
    private static final String PA_PAY_APPEAL_LABEL =
        "You still have to pay for this appeal. You will soon receive a notification with instructions on how to pay by card online.";
    private static final String EU_HU_PAY_APPEAL_LABEL = PA_PAY_APPEAL_LABEL
        + " You need to pay within 14 days of receiving the notification or the Tribunal will end the appeal.";
    private static final String HO_WAIVER_REMISSION_LABEL =
        "You have submitted an appeal with a remission application. Your remission details will be reviewed and you may be asked to "
            + "provide more information. Once the review is complete you will be notified if there is any fee to pay.";
    private static final String REVIEW_LABEL =
        "\n\nOnce you have paid for the appeal, a Tribunal Caseworker will review the reasons your appeal was out of time and you will be "
            + "notified if it can proceed.";
    private static final String OUT_OF_TIME_PNG =
        "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n\n";
    private static final String OUT_OF_TIME_ADMIN_PNG =
            "![Out of time confirmation](https://raw.githubusercontent.com/hmcts/ia-appeal-frontend/master/app/assets/images/outOfTimeConfirmation.png)\n\n";
    private static final String OUT_OF_TIME_WHAT_HAPPENS_NEXT_LABEL = OUT_OF_TIME_PNG + WHAT_HAPPENS_NEXT_LABEL;
    private static final String OUT_OF_TIME_WHAT_HAPPENS_NEXT_ADMIN_LABEL = OUT_OF_TIME_ADMIN_PNG + WHAT_HAPPENS_NEXT_LABEL;
    private static final String OUT_OF_TIME_DO_THIS_NEXT_LABEL = OUT_OF_TIME_PNG + DO_THIS_NEXT_LABEL;
    private static final String DEFAULT_LABEL =
        "You will receive an email confirming that this appeal has been submitted successfully.";
    private static final String ADMIN_LABEL =
        "A Legal Officer will check the appeal is valid and all parties will be notified of next steps.";
    private static final String EJP_LABEL =
            "A Legal Officer will progress the case to the correct state and upload the relevant documents at each point.";
    private static final String OUT_OF_TIME_DEFAULT_LABEL =
        "You have submitted this appeal beyond the deadline. The Tribunal Case Officer will decide if it can proceed. You'll get an email "
            + "telling you whether your appeal can go ahead.";
    private static final String OUT_OF_TIME_ADMIN_LABEL =
            "A Legal Officer will decide if the appeal can proceed.";
    private static final String DEFAULT_HEADER = "# Your appeal has been submitted";
    private static final String ADMIN_HEADER = "# The appeal has been submitted";
    private static final String AGE_ASSESSMENT_APPEAL_INTERIM_LINK =
        "\n\nYou can now apply for [interim relief](#).";




    private final CcdSupplementaryUpdater ccdSupplementaryUpdater;

    public AppealSubmittedConfirmation(CcdSupplementaryUpdater ccdSupplementaryUpdater) {
        this.ccdSupplementaryUpdater = ccdSupplementaryUpdater;
    }

    public boolean canHandle(
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callback, "callback must not be null");

        return callback.getEvent() == Event.SUBMIT_APPEAL;
    }

    public PostSubmitCallbackResponse handle(
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        PostSubmitCallbackResponse postSubmitResponse =
            new PostSubmitCallbackResponse();

        ccdSupplementaryUpdater.setHmctsServiceIdSupplementary(callback);

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        YesOrNo submissionOutOfTime =
            requireNonNull(asylumCase.read(SUBMISSION_OUT_OF_TIME, YesOrNo.class)
                .<RequiredFieldMissingException>orElseThrow(
                    () -> new RequiredFieldMissingException("submission out of time is a required field")));

        Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);

        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("Appeal type is not present"));

        if (isInternalCase(asylumCase)) {
            postSubmitResponse.setConfirmationHeader(
                    submissionOutOfTime == NO ? ADMIN_HEADER : ""
            );
        } else {
            postSubmitResponse.setConfirmationHeader(
                    submissionOutOfTime == NO ? DEFAULT_HEADER : ""
            );
        }

        // Check if this is a detained appeal case that qualifies for CMR message
        if (shouldShowCmrDetainedAppealMessage(asylumCase, appealType, submissionOutOfTime)) {
            setDetainedAppealCmrConfirmation(postSubmitResponse, callback);
            return postSubmitResponse;
        }

        switch (appealType) {

            case EA:
            case HU:
            case EU:
                if (remissionType.isPresent() && remissionType.get() != NO_REMISSION) {

                    setRemissionConfirmation(postSubmitResponse, remissionType.get(), submissionOutOfTime, asylumCase);
                } else if (remissionType.isPresent()
                           && remissionType.get() == NO_REMISSION
                           && !isWaysToPay(isEaHuPaEu(asylumCase), !HandlerUtils.isAipJourney(asylumCase))) {
                    setEaHuAppealTypesConfirmation(postSubmitResponse, asylumCase, submissionOutOfTime);
                } else if (remissionType.isPresent()
                           && remissionType.get() == NO_REMISSION
                           && isWaysToPay(isEaHuPaEu(asylumCase), !HandlerUtils.isAipJourney(asylumCase))) {
                    setWaysToPayLabelEuHuPa(postSubmitResponse, callback, submissionOutOfTime, asylumCase);
                } else {

                    setDefaultConfirmation(postSubmitResponse, submissionOutOfTime, asylumCase);
                }
                break;

            case PA:
                String paymentOption = asylumCase
                    .read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
                    .orElse("");
                if (remissionType.isPresent() && remissionType.get() != NO_REMISSION) {

                    setRemissionConfirmation(postSubmitResponse, remissionType.get(), submissionOutOfTime, asylumCase);
                } else if (remissionType.isPresent()
                           && remissionType.get() == NO_REMISSION
                           && !isWaysToPay(isEaHuPaEu(asylumCase), !HandlerUtils.isAipJourney(asylumCase))) {
                    setPaAppealTypeConfirmation(postSubmitResponse, callback, asylumCase, submissionOutOfTime);
                } else if (remissionType.isPresent()
                           && remissionType.get() == NO_REMISSION
                           && isWaysToPay(isEaHuPaEu(asylumCase), !HandlerUtils.isAipJourney(asylumCase))) {
                    if (paymentOption.equals("payLater")) {
                        setWaysToPayLabelPaPayLater(postSubmitResponse, callback, submissionOutOfTime, asylumCase);
                    } else {
                        setWaysToPayLabelEuHuPa(postSubmitResponse, callback, submissionOutOfTime, asylumCase);
                    }
                } else {

                    setDefaultConfirmation(postSubmitResponse, submissionOutOfTime, asylumCase);
                }
                break;

            case AG:
                setAgAppealTypeConfirmation(postSubmitResponse, submissionOutOfTime, asylumCase);
                break;

            case DC:
                setDefaultConfirmation(postSubmitResponse, submissionOutOfTime, asylumCase);
                break;

            default:
                setDefaultConfirmation(postSubmitResponse, submissionOutOfTime, asylumCase);
        }

        return postSubmitResponse;
    }

    /**
     * Determines if the CMR detained appeal message should be shown.
     * 
     * @param asylumCase the asylum case
     * @param appealType the appeal type
     * @param submissionOutOfTime whether the submission is out of time
     * @return true if CMR message should be shown, false otherwise
     */
    private boolean shouldShowCmrDetainedAppealMessage(AsylumCase asylumCase, AppealType appealType, YesOrNo submissionOutOfTime) {
        // Must be an internal case (Legal Officer)
        if (!isInternalCase(asylumCase)) {
            return false;
        }
        
        // Must be a detained appeal
        if (!isAppellantInDetention(asylumCase)) {
            return false;
        }
        
        // Must be EA, HU, EU, or DC appeal type (excluding RA & PA)
        if (!List.of(EA, HU, EU, DC).contains(appealType)) {
            return false;
        }
        
        // Check payment status - appeal should be paid or no payment required
        if (!isPaymentSatisfied(asylumCase)) {
            return false;
        }
        
        // If out of time, check that recorded decision allows appeal to proceed
        if (submissionOutOfTime == YES) {
            return isOutOfTimeDecisionFavorable(asylumCase);
        }
        
        return true;
    }

    /**
     * Checks if payment requirements are satisfied.
     */
    private boolean isPaymentSatisfied(AsylumCase asylumCase) {
        Optional<PaymentStatus> paymentStatus = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class);
        
        // If payment status is PAID, payment is satisfied
        if (paymentStatus.isPresent() && paymentStatus.get() == PaymentStatus.PAID) {
            return true;
        }
        
        // Check if remission is approved (no payment needed)
        Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
        if (remissionType.isPresent() && remissionType.get() != NO_REMISSION) {
            // For remission cases, check if it's approved
            return asylumCase.read(REMISSION_DECISION, uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.class)
                .map(decision -> decision == uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.APPROVED)
                .orElse(false);
        }
        
        // If no payment status and no remission, assume no payment required for now
        // This may need refinement based on business rules
        return paymentStatus.isEmpty();
    }

    /**
     * Checks if out of time decision is favorable (IN_TIME or APPROVED).
     */
    private boolean isOutOfTimeDecisionFavorable(AsylumCase asylumCase) {
        Optional<YesOrNo> recordedOutOfTimeDecision = asylumCase.read(RECORDED_OUT_OF_TIME_DECISION, YesOrNo.class);
        
        // If no recorded decision yet, assume not favorable
        if (recordedOutOfTimeDecision.isEmpty() || recordedOutOfTimeDecision.get() == NO) {
            return false;
        }
        
        Optional<OutOfTimeDecisionType> outOfTimeDecisionType = asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class);
        
        return outOfTimeDecisionType.isPresent() && 
               (outOfTimeDecisionType.get() == OutOfTimeDecisionType.IN_TIME || 
                outOfTimeDecisionType.get() == OutOfTimeDecisionType.APPROVED);
    }

    /**
     * Sets the confirmation message for detained appeals requiring CMR consideration.
     */
    private void setDetainedAppealCmrConfirmation(PostSubmitCallbackResponse postSubmitResponse, Callback<AsylumCase> callback) {
        String cmrDetainedAppealLabel = 
            "You must review the appeal in the documents tab. Create a listing task if a [CMR is required for this detained appeal](/case/IA/Asylum/" + callback.getCaseDetails().getId() + "/trigger/createCmrListingTask).\n\n"
            + "If a CMR is not required for this detained appeal and the appeal looks valid, you must tell the respondent to supply their evidence\n\n"
            + "[Request respondent evidence](/case/IA/Asylum/" + callback.getCaseDetails().getId() + "/trigger/requestRespondentEvidence).</br>";
        
        postSubmitResponse.setConfirmationBody(DO_THIS_NEXT_LABEL + cmrDetainedAppealLabel);
    }

    private void setEaHuAppealTypesConfirmation(PostSubmitCallbackResponse postSubmitCallbackResponse,
                                                AsylumCase asylumCase, YesOrNo submissionOutOfTime) {

        asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)
            .ifPresent(paymentOption -> {

                if (paymentOption.equals(PAYMENT_OPTION_PAY_OFFLINE) && !isInternalCase(asylumCase)) {

                    postSubmitCallbackResponse.setConfirmationBody(
                        submissionOutOfTime == NO
                            ? WHAT_HAPPENS_NEXT_LABEL + EU_HU_PAY_APPEAL_LABEL
                            : OUT_OF_TIME_WHAT_HAPPENS_NEXT_LABEL + EU_HU_PAY_APPEAL_LABEL + REVIEW_LABEL
                    );
                }

                if (paymentOption.equals(PAYMENT_OPTION_PAY_OFFLINE) && isInternalCase(asylumCase)) {

                    postSubmitCallbackResponse.setConfirmationBody(
                            submissionOutOfTime == NO
                                    ? WHAT_HAPPENS_NEXT_LABEL + EU_HU_PAY_APPEAL_LABEL
                                    : OUT_OF_TIME_WHAT_HAPPENS_NEXT_ADMIN_LABEL + EU_HU_PAY_APPEAL_LABEL + REVIEW_LABEL
                    );
                }
            });
    }

    private void setPaAppealTypeConfirmation(PostSubmitCallbackResponse postSubmitCallbackResponse,
                                             Callback<AsylumCase> callback, AsylumCase asylumCase,
                                             YesOrNo submissionOutOfTime) {

        final String paOverviewTabLabel =
            "[" + "overview tab" + "](/case/IA/Asylum/" + callback.getCaseDetails().getId() + "#overview)";
        final String paPayLaterLabel =
            "You still have to pay for this appeal. You can do this by selecting Make a payment from the dropdown on the "
                + paOverviewTabLabel + " and following the instructions.";

        asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class)
            .ifPresent(paymentOption -> {

                if (paymentOption.equals(PAYMENT_OPTION_PAY_OFFLINE) && !isInternalCase(asylumCase)) {
                    postSubmitCallbackResponse.setConfirmationBody(
                        submissionOutOfTime == NO
                            ? WHAT_HAPPENS_NEXT_LABEL + PA_PAY_APPEAL_LABEL
                            : OUT_OF_TIME_WHAT_HAPPENS_NEXT_LABEL + PA_PAY_APPEAL_LABEL + REVIEW_LABEL
                    );
                } else if (paymentOption.equals(PAYMENT_OPTION_PAY_OFFLINE) && isInternalCase(asylumCase)) {
                    postSubmitCallbackResponse.setConfirmationBody(
                            submissionOutOfTime == NO
                                    ? WHAT_HAPPENS_NEXT_LABEL + PA_PAY_APPEAL_LABEL
                                    : OUT_OF_TIME_WHAT_HAPPENS_NEXT_ADMIN_LABEL + PA_PAY_APPEAL_LABEL + REVIEW_LABEL
                    );
                } else if (paymentOption.equals(PAYMENT_OPTION_PAY_LATER) && !isInternalCase(asylumCase)) {
                    postSubmitCallbackResponse.setConfirmationBody(
                        submissionOutOfTime == NO
                            ? WHAT_HAPPENS_NEXT_LABEL + paPayLaterLabel
                            : OUT_OF_TIME_WHAT_HAPPENS_NEXT_LABEL + paPayLaterLabel + REVIEW_LABEL
                    );
                } else if (paymentOption.equals(PAYMENT_OPTION_PAY_LATER) && isInternalCase(asylumCase)) {
                    postSubmitCallbackResponse.setConfirmationBody(
                            submissionOutOfTime == NO
                                    ? WHAT_HAPPENS_NEXT_LABEL + paPayLaterLabel
                                    : OUT_OF_TIME_WHAT_HAPPENS_NEXT_ADMIN_LABEL + paPayLaterLabel + REVIEW_LABEL
                    );
                }
            });
    }

    private void setRemissionConfirmation(PostSubmitCallbackResponse postSubmitCallbackResponse,
                                          RemissionType remissionType,
                                          YesOrNo submissionOutOfTime,
                                          AsylumCase asylumCase) {

        if (remissionType == HO_WAIVER_REMISSION
            || remissionType == HELP_WITH_FEES
            || remissionType == EXCEPTIONAL_CIRCUMSTANCES_REMISSION) {

            if (isInternalCase(asylumCase)) {
                postSubmitCallbackResponse.setConfirmationBody(
                        submissionOutOfTime == NO
                                ? WHAT_HAPPENS_NEXT_LABEL + HO_WAIVER_REMISSION_LABEL
                                : OUT_OF_TIME_WHAT_HAPPENS_NEXT_ADMIN_LABEL
                                + HO_WAIVER_REMISSION_LABEL
                                +
                                "\n\n\nA Tribunal Caseworker will then review the reasons your appeal was submitted out of time and you will be notified if "
                                + "it can proceed."
                );
            } else {
                postSubmitCallbackResponse.setConfirmationBody(
                        submissionOutOfTime == NO
                                ? WHAT_HAPPENS_NEXT_LABEL + HO_WAIVER_REMISSION_LABEL
                                : OUT_OF_TIME_WHAT_HAPPENS_NEXT_LABEL
                                + HO_WAIVER_REMISSION_LABEL
                                +
                                "\n\n\nA Tribunal Caseworker will then review the reasons your appeal was submitted out of time and you will be notified if "
                                + "it can proceed."
                );
            }
        }
    }

    private void setDefaultConfirmation(PostSubmitCallbackResponse postSubmitCallbackResponse,
                                        YesOrNo submissionOutOfTime,
                                        AsylumCase asylumCase) {
        if (isInternalCase(asylumCase)) {
            if (isEjpCase(asylumCase)) {
                postSubmitCallbackResponse.setConfirmationBody(WHAT_HAPPENS_NEXT_LABEL + EJP_LABEL);
            } else {
                postSubmitCallbackResponse.setConfirmationBody(
                        submissionOutOfTime == NO
                                ? WHAT_HAPPENS_NEXT_LABEL + ADMIN_LABEL
                                : OUT_OF_TIME_WHAT_HAPPENS_NEXT_ADMIN_LABEL + OUT_OF_TIME_ADMIN_LABEL
                );
            }
        } else {
            postSubmitCallbackResponse.setConfirmationBody(
                    submissionOutOfTime == NO
                            ? WHAT_HAPPENS_NEXT_LABEL + DEFAULT_LABEL
                            : OUT_OF_TIME_WHAT_HAPPENS_NEXT_LABEL + OUT_OF_TIME_DEFAULT_LABEL
            );
        }
    }


    private void setAgAppealTypeConfirmation(PostSubmitCallbackResponse postSubmitCallbackResponse,
                                             YesOrNo submissionOutOfTime,
                                             AsylumCase asylumCase) {

        if (isInternalCase(asylumCase)) {
            postSubmitCallbackResponse.setConfirmationBody(
                    submissionOutOfTime == NO
                            ? WHAT_HAPPENS_NEXT_LABEL + DEFAULT_LABEL + AGE_ASSESSMENT_APPEAL_INTERIM_LINK
                            : OUT_OF_TIME_WHAT_HAPPENS_NEXT_ADMIN_LABEL + OUT_OF_TIME_ADMIN_LABEL + AGE_ASSESSMENT_APPEAL_INTERIM_LINK
            );
        } else {
            postSubmitCallbackResponse.setConfirmationBody(
                    submissionOutOfTime == NO
                            ? WHAT_HAPPENS_NEXT_LABEL + DEFAULT_LABEL + AGE_ASSESSMENT_APPEAL_INTERIM_LINK
                            : OUT_OF_TIME_WHAT_HAPPENS_NEXT_LABEL + OUT_OF_TIME_DEFAULT_LABEL + AGE_ASSESSMENT_APPEAL_INTERIM_LINK
            );
        }
    }

    private void setWaysToPayLabelEuHuPa(PostSubmitCallbackResponse postSubmitCallbackResponse,
                                         Callback<AsylumCase> callback,
                                         YesOrNo submissionOutOfTime,
                                         AsylumCase asylumCase) {

        String payForAppeal = "You must now pay for this appeal. First [create a service request](/case/IA/Asylum/"
                + callback.getCaseDetails().getId() + "/trigger/generateServiceRequest), you can do this by "
                + "selecting 'Create a service request' from the 'Next step' dropdown list. Then select 'Go'.\n\n";

        if (isInternalCase(asylumCase)) {
            postSubmitCallbackResponse.setConfirmationBody(
                    submissionOutOfTime == NO
                            ? WHAT_HAPPENS_NEXT_LABEL + payForAppeal
                            : OUT_OF_TIME_WHAT_HAPPENS_NEXT_ADMIN_LABEL + payForAppeal + REVIEW_LABEL
            );
        } else {
            postSubmitCallbackResponse.setConfirmationBody(
                    submissionOutOfTime == NO
                            ? DO_THIS_NEXT_LABEL + payForAppeal
                            : OUT_OF_TIME_DO_THIS_NEXT_LABEL + payForAppeal + REVIEW_LABEL
            );
        }
    }

    private void setWaysToPayLabelPaPayLater(PostSubmitCallbackResponse postSubmitCallbackResponse,
                                         Callback<AsylumCase> callback,
                                         YesOrNo submissionOutOfTime,
                                         AsylumCase asylumCase) {

        String paPayLaterLabel = "You still have to pay for this appeal. First [create a service request](/case/IA/Asylum/"
            + callback.getCaseDetails().getId() + "/trigger/generateServiceRequest), you can do this by "
            + "selecting 'Create a service request' from the 'Next step' dropdown list. Then select 'Go'.\n\n";

        if (isInternalCase(asylumCase)) {
            postSubmitCallbackResponse.setConfirmationBody(
                    submissionOutOfTime == NO
                            ? WHAT_HAPPENS_NEXT_LABEL + paPayLaterLabel
                            : OUT_OF_TIME_WHAT_HAPPENS_NEXT_ADMIN_LABEL + paPayLaterLabel + REVIEW_LABEL
            );
        } else {
            postSubmitCallbackResponse.setConfirmationBody(
                    submissionOutOfTime == NO
                            ? WHAT_HAPPENS_NEXT_LABEL + paPayLaterLabel
                            : OUT_OF_TIME_WHAT_HAPPENS_NEXT_LABEL + paPayLaterLabel + REVIEW_LABEL
            );
        }

    }

    private boolean isWaysToPay(boolean isHuOrEaOrPa, boolean isLegalRepJourney) {
        return isHuOrEaOrPa && isLegalRepJourney;
    }

    private boolean isEaHuPaEu(AsylumCase asylumCase) {
        Optional<AppealType> optionalAppealType = asylumCase.read(APPEAL_TYPE, AppealType.class);
        if (optionalAppealType.isPresent()) {
            AppealType appealType = optionalAppealType.get();
            return List.of(EA, HU, PA, EU).contains(appealType);
        }
        return false;
    }
}
