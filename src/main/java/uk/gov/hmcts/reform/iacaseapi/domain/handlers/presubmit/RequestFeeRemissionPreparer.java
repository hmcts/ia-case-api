package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_DECISION_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAipJourney;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RemissionDetailsAppender;

@Component
public class RequestFeeRemissionPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final RemissionDetailsAppender remissionDetailsAppender;

    public RequestFeeRemissionPreparer(
        FeatureToggler featureToggler,
        RemissionDetailsAppender remissionDetailsAppender
    ) {
        this.featureToggler = featureToggler;
        this.remissionDetailsAppender = remissionDetailsAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.REQUEST_FEE_REMISSION
               && !isAipJourney(callback.getCaseDetails().getCaseData())
               && featureToggler.getValue("remissions-feature", false);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final PreSubmitCallbackResponse<AsylumCase> callbackResponse = new PreSubmitCallbackResponse<>(asylumCase);

        final AppealType appealType = asylumCase.read(AsylumCaseFieldDefinition.APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("Appeal type is not present"));

        switch (appealType) {
            case DC:
            case RP:

                callbackResponse.addError("You cannot request a fee remission for this appeal");
                break;

            case EA:
            case HU:
            case PA:

                Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
                Optional<RemissionType> lateRemissionType = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);
                Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

                if ((remissionType.isPresent() && remissionType.get() != RemissionType.NO_REMISSION && !remissionDecision.isPresent())
                    || (lateRemissionType.isPresent() && !remissionDecision.isPresent())) {

                    callbackResponse
                        .addError("You cannot request a fee remission at this time because another fee remission request for this appeal "
                                  + "has yet to be decided.");
                } else if (isPreviousRemissionExists(remissionType, remissionDecision)
                           || isPreviousRemissionExists(lateRemissionType, remissionDecision)) {

                    appendPreviousRemissionDetails(asylumCase);
                    asylumCase.clear(REMISSION_TYPE);
                }
                break;

            default:
                break;
        }

        return callbackResponse;
    }

    private boolean isPreviousRemissionExists(Optional<RemissionType> remissionType, Optional<RemissionDecision> remissionDecision) {

        return remissionType.isPresent()
               && remissionType.get() != RemissionType.NO_REMISSION
               && remissionDecision.isPresent()
               && Arrays.asList(APPROVED, PARTIALLY_APPROVED, REJECTED)
                   .contains(remissionDecision.get());
    }

    private void appendPreviousRemissionDetails(AsylumCase asylumCase) {

        List<IdValue<RemissionDetails>> previousRemissionDetails = null;

        String feeRemissionType = asylumCase.read(FEE_REMISSION_TYPE, String.class)
            .orElseThrow(() -> new IllegalStateException("Previous fee remission type is not present"));
        Optional<List<IdValue<RemissionDetails>>> maybeExistingRemissionDetails = asylumCase.read(PREVIOUS_REMISSION_DETAILS);
        final List<IdValue<RemissionDetails>> existingRemissionDetails = maybeExistingRemissionDetails.orElse(Collections.emptyList());

        switch (feeRemissionType) {
            case "Asylum support":
                String asylumSupportReference = asylumCase.read(ASYLUM_SUPPORT_REFERENCE, String.class)
                    .orElse("");
                Optional<Document>  asylumSupportDocument = asylumCase.read(ASYLUM_SUPPORT_DOCUMENT);

                previousRemissionDetails =
                    asylumSupportDocument.isPresent()
                        ? remissionDetailsAppender.appendAsylumSupportRemissionDetails(
                            existingRemissionDetails, feeRemissionType, asylumSupportReference, asylumSupportDocument.get())
                        : remissionDetailsAppender.appendAsylumSupportRemissionDetails(
                            existingRemissionDetails, feeRemissionType, asylumSupportReference, null);

                break;

            case "Legal Aid":
                String legalAidAccountNumber = asylumCase.read(LEGAL_AID_ACCOUNT_NUMBER, String.class)
                    .orElse("");

                previousRemissionDetails =
                    remissionDetailsAppender
                        .appendLegalAidRemissionDetails(existingRemissionDetails, feeRemissionType, legalAidAccountNumber);

                break;

            case "Section 17":
                Optional<Document> section17Document = asylumCase.read(SECTION17_DOCUMENT);

                if (section17Document.isPresent()) {

                    previousRemissionDetails =
                        remissionDetailsAppender
                            .appendSection17RemissionDetails(existingRemissionDetails, feeRemissionType, section17Document.get());
                }

                break;

            case "Section 20":
                Optional<Document> section20Document = asylumCase.read(SECTION20_DOCUMENT);

                if (section20Document.isPresent()) {

                    previousRemissionDetails =
                        remissionDetailsAppender
                            .appendSection20RemissionDetails(existingRemissionDetails, feeRemissionType, section20Document.get());
                }

                break;

            case "Home Office fee waiver":
                Optional<Document> homeWaiverDocument = asylumCase.read(HOME_OFFICE_WAIVER_DOCUMENT);

                if (homeWaiverDocument.isPresent()) {

                    previousRemissionDetails =
                        remissionDetailsAppender
                            .appendHomeOfficeWaiverRemissionDetails(existingRemissionDetails, feeRemissionType, homeWaiverDocument.get());
                }

                break;

            case "Help with Fees":
                String helpWithReference = asylumCase.read(HELP_WITH_FEES_REFERENCE_NUMBER, String.class).orElse("");

                previousRemissionDetails =
                    remissionDetailsAppender
                        .appendHelpWithFeeReferenceRemissionDetails(existingRemissionDetails, feeRemissionType, helpWithReference);

                break;

            case "Exceptional circumstances":

                String exceptionalCircumstances = asylumCase.read(EXCEPTIONAL_CIRCUMSTANCES, String.class)
                    .orElseThrow(() -> new IllegalStateException("Exceptional circumstances details not present"));
                Optional<List<IdValue<Document>>> exceptionalCircumstancesDocuments = asylumCase.read(REMISSION_EC_EVIDENCE_DOCUMENTS);

                previousRemissionDetails =
                    exceptionalCircumstancesDocuments.isPresent()
                        ? remissionDetailsAppender.appendExceptionalCircumstancesRemissionDetails(
                            existingRemissionDetails, feeRemissionType, exceptionalCircumstances, exceptionalCircumstancesDocuments.get())
                        : remissionDetailsAppender.appendExceptionalCircumstancesRemissionDetails(
                            existingRemissionDetails, feeRemissionType, exceptionalCircumstances, null);

                break;

            default:
                break;
        }

        if (previousRemissionDetails != null) {

            appendPreviousRemissionDecisionDetails(previousRemissionDetails, asylumCase);
        }
    }

    private void appendPreviousRemissionDecisionDetails(List<IdValue<RemissionDetails>> previousRemissionDetails, AsylumCase asylumCase) {

        RemissionDecision remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
            .orElseThrow(() -> new IllegalStateException("Remission decision is not present"));
        String feeAmount = asylumCase.read(FEE_AMOUNT_GBP, String.class).orElse("");

        previousRemissionDetails
            .stream()
            .forEach(idValue -> {

                RemissionDetails remissionDetails = idValue.getValue();

                if (remissionDetails.getRemissionDecision() == null) {

                    remissionDetails.setFeeAmount(feeAmount);

                    switch (remissionDecision) {
                        case APPROVED:
                        case PARTIALLY_APPROVED:

                            String amountRemitted = asylumCase.read(AMOUNT_REMITTED, String.class).orElse("");
                            String amountLeftToPay = asylumCase.read(AMOUNT_LEFT_TO_PAY, String.class).orElse("");
                            remissionDetails.setAmountRemitted(amountRemitted);
                            remissionDetails.setAmountLeftToPay(amountLeftToPay);

                            if (remissionDecision == APPROVED) {

                                remissionDetails.setRemissionDecision("Approved");
                            } else {

                                String remissionDecisionReason = asylumCase.read(REMISSION_DECISION_REASON, String.class).orElse("");
                                remissionDetails.setRemissionDecision("Partially approved");
                                remissionDetails.setRemissionDecisionReason(remissionDecisionReason);
                            }

                            break;

                        case REJECTED:

                            String remissionDecisionReason = asylumCase.read(REMISSION_DECISION_REASON, String.class).orElse("");
                            remissionDetails.setRemissionDecision("Rejected");
                            remissionDetails.setRemissionDecisionReason(remissionDecisionReason);
                            break;

                        default:
                            break;
                    }
                }
            });

        remissionDetailsAppender.setRemissions(previousRemissionDetails);
    }
}
