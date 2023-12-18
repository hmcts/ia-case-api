package uk.gov.hmcts.reform.iacaseapi.domain.handlers.postsubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.HELP_WITH_FEES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.HO_WAIVER_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.NO_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PostSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PostSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AsylumCasePostFeePaymentService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Scheduler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.CcdSupplementaryUpdater;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.TimedEvent;

@Slf4j
@Component
public class AppealSubmittedConfirmation implements PostSubmitCallbackHandler<AsylumCase> {

    private static final String PAYMENT_OPTION_PAY_OFFLINE = "payOffline";
    private static final String PAYMENT_OPTION_PAY_LATER = "payLater";
    private static final String WHAT_HAPPENS_NEXT_LABEL = "#### What happens next\n\n";
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
    private static final String OUT_OF_TIME_WHAT_HAPPENS_NEXT_LABEL = OUT_OF_TIME_PNG + WHAT_HAPPENS_NEXT_LABEL;
    private static final String DEFAULT_LABEL =
        "You will receive an email confirming that this appeal has been submitted successfully.";
    private static final String OUT_OF_TIME_DEFAULT_LABEL =
        "You have submitted this appeal beyond the deadline. The Tribunal Case Officer will decide if it can proceed. You'll get an email "
            + "telling you whether your appeal can go ahead.";
    private static final String DEFAULT_HEADER = "# Your appeal has been submitted";


    private final CcdSupplementaryUpdater ccdSupplementaryUpdater;
    private final AsylumCasePostFeePaymentService asylumCasePostFeePaymentService;
    private final Scheduler scheduler;

    public AppealSubmittedConfirmation(AsylumCasePostFeePaymentService asylumCasePostFeePaymentService, CcdSupplementaryUpdater ccdSupplementaryUpdater, Scheduler scheduler) {
        this.asylumCasePostFeePaymentService = asylumCasePostFeePaymentService;
        this.ccdSupplementaryUpdater = ccdSupplementaryUpdater;
        this.scheduler = scheduler;
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

        if (!HandlerUtils.isAipJourney(asylumCase)) {
            sendPaymentCallback(callback);
        }

        postSubmitResponse.setConfirmationHeader(
            submissionOutOfTime == NO ? DEFAULT_HEADER : ""
        );

        switch (appealType) {

            case EA:
            case HU:
            case EU:
                if (remissionType.isPresent() && remissionType.get() != NO_REMISSION) {

                    setRemissionConfirmation(postSubmitResponse, remissionType.get(), submissionOutOfTime);
                } else if (remissionType.isPresent()
                           && remissionType.get() == NO_REMISSION
                           && !isWaysToPay(isEaHuPaEu(asylumCase), !HandlerUtils.isAipJourney(asylumCase))) {
                    setEaHuAppealTypesConfirmation(postSubmitResponse, asylumCase, submissionOutOfTime);
                } else if (remissionType.isPresent()
                           && remissionType.get() == NO_REMISSION
                           && isWaysToPay(isEaHuPaEu(asylumCase), !HandlerUtils.isAipJourney(asylumCase))) {
                    scheduleCreateServiceRequest(callback);
                    setWaysToPayLabelEuHuPa(postSubmitResponse, callback, submissionOutOfTime);
                } else {

                    setDefaultConfirmation(postSubmitResponse, submissionOutOfTime);
                }
                break;

            case PA:
                if (remissionType.isPresent() && remissionType.get() != NO_REMISSION) {

                    setRemissionConfirmation(postSubmitResponse, remissionType.get(), submissionOutOfTime);
                } else if (remissionType.isPresent()
                           && remissionType.get() == NO_REMISSION
                           && !isWaysToPay(isEaHuPaEu(asylumCase), !HandlerUtils.isAipJourney(asylumCase))) {
                    setPaAppealTypeConfirmation(postSubmitResponse, callback, asylumCase, submissionOutOfTime);
                } else if (remissionType.isPresent()
                           && remissionType.get() == NO_REMISSION
                           && isWaysToPay(isEaHuPaEu(asylumCase), !HandlerUtils.isAipJourney(asylumCase))) {
                    scheduleCreateServiceRequest(callback);
                    setWaysToPayLabelPaPayNowPayLater(postSubmitResponse, callback, submissionOutOfTime);
                } else {

                    setDefaultConfirmation(postSubmitResponse, submissionOutOfTime);
                }
                break;

            default:
                setDefaultConfirmation(postSubmitResponse, submissionOutOfTime);
        }

        return postSubmitResponse;
    }

    private void setEaHuAppealTypesConfirmation(PostSubmitCallbackResponse postSubmitCallbackResponse,
                                                AsylumCase asylumCase, YesOrNo submissionOutOfTime) {

        asylumCase.read(EA_HU_APPEAL_TYPE_PAYMENT_OPTION, String.class)
            .ifPresent(paymentOption -> {

                if (paymentOption.equals(PAYMENT_OPTION_PAY_OFFLINE)) {

                    postSubmitCallbackResponse.setConfirmationBody(
                        submissionOutOfTime == NO
                            ? WHAT_HAPPENS_NEXT_LABEL + EU_HU_PAY_APPEAL_LABEL
                            : OUT_OF_TIME_WHAT_HAPPENS_NEXT_LABEL + EU_HU_PAY_APPEAL_LABEL + REVIEW_LABEL
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

                if (paymentOption.equals(PAYMENT_OPTION_PAY_OFFLINE)) {
                    postSubmitCallbackResponse.setConfirmationBody(
                        submissionOutOfTime == NO
                            ? WHAT_HAPPENS_NEXT_LABEL + PA_PAY_APPEAL_LABEL
                            : OUT_OF_TIME_WHAT_HAPPENS_NEXT_LABEL + PA_PAY_APPEAL_LABEL + REVIEW_LABEL
                    );
                } else if (paymentOption.equals(PAYMENT_OPTION_PAY_LATER)) {
                    postSubmitCallbackResponse.setConfirmationBody(
                        submissionOutOfTime == NO
                            ? WHAT_HAPPENS_NEXT_LABEL + paPayLaterLabel
                            : OUT_OF_TIME_WHAT_HAPPENS_NEXT_LABEL + paPayLaterLabel + REVIEW_LABEL
                    );
                }
            });
    }

    private void setRemissionConfirmation(PostSubmitCallbackResponse postSubmitCallbackResponse,
                                          RemissionType remissionType,
                                          YesOrNo submissionOutOfTime) {

        if (remissionType == HO_WAIVER_REMISSION
            || remissionType == HELP_WITH_FEES
            || remissionType == EXCEPTIONAL_CIRCUMSTANCES_REMISSION) {

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

    private void setDefaultConfirmation(PostSubmitCallbackResponse postSubmitCallbackResponse,
                                        YesOrNo submissionOutOfTime) {

        postSubmitCallbackResponse.setConfirmationBody(
            submissionOutOfTime == NO
                ? WHAT_HAPPENS_NEXT_LABEL + DEFAULT_LABEL
                : OUT_OF_TIME_WHAT_HAPPENS_NEXT_LABEL + OUT_OF_TIME_DEFAULT_LABEL
        );
    }

    private void setWaysToPayLabelEuHuPa(PostSubmitCallbackResponse postSubmitCallbackResponse,
                                         Callback<AsylumCase> callback,
                                         YesOrNo submissionOutOfTime) {

        String payForAppeal = "You need to pay for your appeal.\n\n[Pay for appeal](cases/case-details/"
                                     + callback.getCaseDetails().getId() + "#Service%20Request)\n\n";

        postSubmitCallbackResponse.setConfirmationBody(
            submissionOutOfTime == NO
            ? WHAT_HAPPENS_NEXT_LABEL + payForAppeal
            : OUT_OF_TIME_WHAT_HAPPENS_NEXT_LABEL + payForAppeal + REVIEW_LABEL
        );
    }

    private void setWaysToPayLabelPaPayNowPayLater(PostSubmitCallbackResponse postSubmitCallbackResponse,
                                         Callback<AsylumCase> callback,
                                         YesOrNo submissionOutOfTime) {

        String paPayNowPayLaterLabel = "You still have to pay for this appeal.\n\nYou can do this by selecting [Pay for appeal](cases/case-details/"
                              + callback.getCaseDetails().getId() + "#Service%20Request)\n\n";

        postSubmitCallbackResponse.setConfirmationBody(
            submissionOutOfTime == NO
                ? WHAT_HAPPENS_NEXT_LABEL + paPayNowPayLaterLabel
                : OUT_OF_TIME_WHAT_HAPPENS_NEXT_LABEL + paPayNowPayLaterLabel + REVIEW_LABEL
        );
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

    private void sendPaymentCallback(Callback<AsylumCase> callback) {

        Callback<AsylumCase> callbackForPaymentApi = new Callback<>(
            callback.getCaseDetails(),
            callback.getCaseDetailsBefore(),
            Event.SUBMIT_APPEAL
        );
        log.debug("PostSubmit Callback to ia-case-payments-api to generate service request");
        asylumCasePostFeePaymentService.ccdSubmitted(callbackForPaymentApi);

    }

    private void scheduleCreateServiceRequest(Callback<AsylumCase> callback) {
        ZonedDateTime scheduledDate = ZonedDateTime.now();
        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        String[] splitCcdReference = asylumCase.read(CCD_REFERENCE_NUMBER_FOR_DISPLAY, String.class).orElse("").split(" ");
        String ccdReference = String.join("", splitCcdReference);

        scheduler.schedule(
                new TimedEvent(
                        "",
                        Event.CREATE_SERVICE_REQUEST,
                        scheduledDate,
                        "IA",
                        "Asylum",
                        Long.parseLong(ccdReference)
                )
        );
    }
}
