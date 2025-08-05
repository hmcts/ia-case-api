package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.APPROVED;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAipJourney;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RemissionDetailsAppender;

@Component
@Slf4j
public class RequestFeeRemissionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final RemissionDetailsAppender remissionDetailsAppender;

    public RequestFeeRemissionHandler(
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

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
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

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        final AppealType appealType = asylumCase.read(AsylumCaseFieldDefinition.APPEAL_TYPE, AppealType.class)
                .orElseThrow(() -> new IllegalStateException("Appeal type is not present"));

        Optional<List<IdValue<RemissionDetails>>> tempPreviousRemissionDetailsOpt =
                asylumCase.read(TEMP_PREVIOUS_REMISSION_DETAILS);
        List<IdValue<RemissionDetails>> tempPreviousRemissionDetails =
                tempPreviousRemissionDetailsOpt.orElse(emptyList());
        log.info("Handle: getting temp previous remission details: " + tempPreviousRemissionDetails);

        setFeeRemissionTypeDetails(asylumCase);

        switch (appealType) {
            case EA, HU, PA, EU -> {
                appendTempPreviousRemissionDecisionDetails(tempPreviousRemissionDetails, asylumCase);
                asylumCase.write(PREVIOUS_REMISSION_DETAILS, tempPreviousRemissionDetails);
                appendTempPreviousRemissionDetails(asylumCase);
            }
            default -> asylumCase.write(PREVIOUS_REMISSION_DETAILS, tempPreviousRemissionDetails);
        }

        clearPreviousRemissionCaseFields(asylumCase);

        asylumCase.write(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void setFeeRemissionTypeDetails(AsylumCase asylumCase) {

        log.info("Setting fee remission type details");

        Optional<RemissionType> lateRemissionTypeOpt = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);
        String remissionClaim = asylumCase.read(REMISSION_CLAIM, String.class)
            .orElse("");

        if (lateRemissionTypeOpt.isPresent()) {
            RemissionType lateRemissionType = lateRemissionTypeOpt.get();
            asylumCase.write(REMISSION_TYPE, lateRemissionType);
            if (lateRemissionType == RemissionType.HO_WAIVER_REMISSION) {
                switch (remissionClaim) {
                    case "asylumSupport":
                        asylumCase.write(FEE_REMISSION_TYPE, "Asylum support");
                        break;

                    case "legalAid":
                        asylumCase.write(FEE_REMISSION_TYPE, "Legal Aid");
                        break;

                    case "section17":
                        asylumCase.write(FEE_REMISSION_TYPE, "Section 17");
                        break;

                    case "section20":
                        asylumCase.write(FEE_REMISSION_TYPE, "Section 20");
                        break;

                    case "homeOfficeWaiver":
                        asylumCase.write(FEE_REMISSION_TYPE, "Home Office fee waiver");
                        break;

                    default:
                        break;
                }
            } else if (lateRemissionType == RemissionType.HELP_WITH_FEES) {
                asylumCase.write(FEE_REMISSION_TYPE, "Help with Fees");
            } else if (lateRemissionType == RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION) {
                asylumCase.write(FEE_REMISSION_TYPE, "Exceptional circumstances");
            }
        }
    }

    private void clearPreviousRemissionCaseFields(AsylumCase asylumCase) {

        log.info("Clearing previous remission case fields");

        final Optional<RemissionType> lateRemissionTypeOpt = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);
        String remissionClaim = asylumCase.read(REMISSION_CLAIM, String.class).orElse("");

        if (lateRemissionTypeOpt.isPresent()) {

            switch (lateRemissionTypeOpt.get()) {
                case HO_WAIVER_REMISSION:
                    switch (remissionClaim) {
                        case "asylumSupport" -> {
                            clearLegalAidAccountNumberRemissionDetails(asylumCase);
                            clearSection17RemissionDetails(asylumCase);
                            clearSection20RemissionDetails(asylumCase);
                            clearHomeOfficeWaiverRemissionDetails(asylumCase);
                            clearHelpWithFeesRemissionDetails(asylumCase);
                            clearExceptionalCircumstancesRemissionDetails(asylumCase);
                        }
                        case "legalAid" -> {
                            clearAsylumSupportRemissionDetails(asylumCase);
                            clearSection17RemissionDetails(asylumCase);
                            clearSection20RemissionDetails(asylumCase);
                            clearHomeOfficeWaiverRemissionDetails(asylumCase);
                            clearHelpWithFeesRemissionDetails(asylumCase);
                            clearExceptionalCircumstancesRemissionDetails(asylumCase);
                        }
                        case "section17" -> {
                            clearAsylumSupportRemissionDetails(asylumCase);
                            clearLegalAidAccountNumberRemissionDetails(asylumCase);
                            clearSection20RemissionDetails(asylumCase);
                            clearHomeOfficeWaiverRemissionDetails(asylumCase);
                            clearHelpWithFeesRemissionDetails(asylumCase);
                            clearExceptionalCircumstancesRemissionDetails(asylumCase);
                        }
                        case "section20" -> {
                            clearAsylumSupportRemissionDetails(asylumCase);
                            clearLegalAidAccountNumberRemissionDetails(asylumCase);
                            clearSection17RemissionDetails(asylumCase);
                            clearHomeOfficeWaiverRemissionDetails(asylumCase);
                            clearHelpWithFeesRemissionDetails(asylumCase);
                            clearExceptionalCircumstancesRemissionDetails(asylumCase);
                        }
                        case "homeOfficeWaiver" -> {
                            clearAsylumSupportRemissionDetails(asylumCase);
                            clearLegalAidAccountNumberRemissionDetails(asylumCase);
                            clearSection17RemissionDetails(asylumCase);
                            clearSection20RemissionDetails(asylumCase);
                            clearHelpWithFeesRemissionDetails(asylumCase);
                            clearExceptionalCircumstancesRemissionDetails(asylumCase);
                        }
                        default -> {

                        }
                    }
                    break;

                case HELP_WITH_FEES:
                    clearAsylumSupportRemissionDetails(asylumCase);
                    clearLegalAidAccountNumberRemissionDetails(asylumCase);
                    clearSection17RemissionDetails(asylumCase);
                    clearSection20RemissionDetails(asylumCase);
                    clearHomeOfficeWaiverRemissionDetails(asylumCase);
                    clearExceptionalCircumstancesRemissionDetails(asylumCase);
                    break;

                case EXCEPTIONAL_CIRCUMSTANCES_REMISSION:
                    clearAsylumSupportRemissionDetails(asylumCase);
                    clearLegalAidAccountNumberRemissionDetails(asylumCase);
                    clearSection17RemissionDetails(asylumCase);
                    clearSection20RemissionDetails(asylumCase);
                    clearHomeOfficeWaiverRemissionDetails(asylumCase);
                    clearHelpWithFeesRemissionDetails(asylumCase);
                    break;

                default:
                    break;
            }

            asylumCase.clear(REMISSION_DECISION);
            asylumCase.clear(AMOUNT_REMITTED);
            asylumCase.clear(AMOUNT_LEFT_TO_PAY);
            asylumCase.clear(REMISSION_DECISION_REASON);
            asylumCase.clear(REMISSION_TYPE);
        }
    }

    private void clearAsylumSupportRemissionDetails(AsylumCase asylumCase) {
        asylumCase.clear(ASYLUM_SUPPORT_REFERENCE);
        asylumCase.clear(ASYLUM_SUPPORT_DOCUMENT);
    }

    private void clearLegalAidAccountNumberRemissionDetails(AsylumCase asylumCase) {
        asylumCase.clear(LEGAL_AID_ACCOUNT_NUMBER);
    }

    private void clearSection17RemissionDetails(AsylumCase asylumCase) {
        asylumCase.clear(SECTION17_DOCUMENT);
    }

    private void clearSection20RemissionDetails(AsylumCase asylumCase) {
        asylumCase.clear(SECTION20_DOCUMENT);
    }

    private void clearHomeOfficeWaiverRemissionDetails(AsylumCase asylumCase) {
        asylumCase.clear(HOME_OFFICE_WAIVER_DOCUMENT);
    }

    private void clearHelpWithFeesRemissionDetails(AsylumCase asylumCase) {
        asylumCase.clear(HELP_WITH_FEES_REFERENCE_NUMBER);
    }

    private void clearExceptionalCircumstancesRemissionDetails(AsylumCase asylumCase) {
        asylumCase.clear(EXCEPTIONAL_CIRCUMSTANCES);
        asylumCase.clear(REMISSION_EC_EVIDENCE_DOCUMENTS);
    }

    private void appendTempPreviousRemissionDetails(AsylumCase asylumCase) {
        List<IdValue<RemissionDetails>> tempPreviousRemissionDetails = null;

        Optional<List<IdValue<RemissionDetails>>> maybeExistingRemissionDetails =
                asylumCase.read(TEMP_PREVIOUS_REMISSION_DETAILS);
        List<IdValue<RemissionDetails>> existingRemissionDetails = maybeExistingRemissionDetails.orElse(emptyList());
        log.info("Append: getting temp previous remission details: {}", existingRemissionDetails);

        Optional<String> feeRemissionTypeOpt = asylumCase.read(FEE_REMISSION_TYPE, String.class);
        log.info("Fee remission type: {}", feeRemissionTypeOpt);

        if (feeRemissionTypeOpt.isPresent()) {
            String feeRemissionType = feeRemissionTypeOpt.get();

            switch (feeRemissionType) {
                case "Asylum support":
                    String asylumSupportReference = asylumCase.read(ASYLUM_SUPPORT_REFERENCE, String.class)
                            .orElse("");
                    Optional<Document> asylumSupportDocument = asylumCase.read(ASYLUM_SUPPORT_DOCUMENT);

                    tempPreviousRemissionDetails =
                            remissionDetailsAppender.appendAsylumSupportRemissionDetails(
                                    existingRemissionDetails,
                                    feeRemissionType,
                                    asylumSupportReference,
                                    asylumSupportDocument.orElse(null)
                            );
                    break;

                case "Legal Aid":
                    String legalAidAccountNumber = asylumCase.read(LEGAL_AID_ACCOUNT_NUMBER, String.class)
                            .orElse("");

                    tempPreviousRemissionDetails =
                            remissionDetailsAppender.appendLegalAidRemissionDetails(
                                    existingRemissionDetails,
                                    feeRemissionType,
                                    legalAidAccountNumber
                            );
                    break;

                case "Section 17":
                    Optional<Document> section17Document = asylumCase.read(SECTION17_DOCUMENT);

                    if (section17Document.isPresent()) {
                        tempPreviousRemissionDetails =
                                remissionDetailsAppender.appendSection17RemissionDetails(
                                        existingRemissionDetails,
                                        feeRemissionType,
                                        section17Document.get()
                                );
                    }

                    break;

                case "Section 20":
                    Optional<Document> section20Document = asylumCase.read(SECTION20_DOCUMENT);

                    if (section20Document.isPresent()) {
                        tempPreviousRemissionDetails =
                                remissionDetailsAppender.appendSection20RemissionDetails(
                                        existingRemissionDetails,
                                        feeRemissionType,
                                        section20Document.get()
                                );
                    }

                    break;

                case "Home Office fee waiver":
                    Optional<Document> homeOfficeWaiverDocument = asylumCase.read(HOME_OFFICE_WAIVER_DOCUMENT);

                    if (homeOfficeWaiverDocument.isPresent()) {
                        tempPreviousRemissionDetails =
                                remissionDetailsAppender.appendHomeOfficeWaiverRemissionDetails(
                                        existingRemissionDetails,
                                        feeRemissionType,
                                        homeOfficeWaiverDocument.get()
                                );
                    }
                    break;

                case "Help with Fees":
                    String helpWithReference =
                            asylumCase.read(HELP_WITH_FEES_REFERENCE_NUMBER, String.class).orElse("");

                    tempPreviousRemissionDetails =
                            remissionDetailsAppender.appendHelpWithFeeReferenceRemissionDetails(
                                    existingRemissionDetails,
                                    feeRemissionType,
                                    helpWithReference
                            );
                    break;

                case "Exceptional circumstances":
                    String exceptionalCircumstances = asylumCase.read(EXCEPTIONAL_CIRCUMSTANCES, String.class)
                            .orElseThrow(() -> new IllegalStateException("Exceptional circumstances details not present"));
                    Optional<List<IdValue<Document>>> exceptionalCircumstancesDocuments =
                            asylumCase.read(REMISSION_EC_EVIDENCE_DOCUMENTS);

                    tempPreviousRemissionDetails =
                            remissionDetailsAppender.appendExceptionalCircumstancesRemissionDetails(
                                    existingRemissionDetails,
                                    feeRemissionType,
                                    exceptionalCircumstances,
                                    exceptionalCircumstancesDocuments.orElse(null)
                            );
                    break;

                default:
                    break;
            }
        }

        asylumCase.write(TEMP_PREVIOUS_REMISSION_DETAILS, tempPreviousRemissionDetails);
    }

    private void appendTempPreviousRemissionDecisionDetails(
        List<IdValue<RemissionDetails>> tempPreviousRemissionDetails,
        AsylumCase asylumCase
    ) {
        log.info("Appending previous remission decision details");

        Optional<RemissionDecision> remissionDecisionOpt = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

        String feeAmount = asylumCase.read(FEE_AMOUNT_GBP, String.class).orElse("");

        tempPreviousRemissionDetails.forEach(
            idValue -> {
                RemissionDetails remissionDetails = idValue.getValue();

                if (remissionDetails.getRemissionDecision() == null) {
                    remissionDetails.setFeeAmount(feeAmount);

                    if (remissionDecisionOpt.isPresent()) {
                        RemissionDecision remissionDecision = remissionDecisionOpt.get();

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
                                    String remissionDecisionReason =
                                            asylumCase.read(REMISSION_DECISION_REASON, String.class).orElse("");
                                    remissionDetails.setRemissionDecision("Partially approved");
                                    remissionDetails.setRemissionDecisionReason(remissionDecisionReason);
                                }
                                break;

                            case REJECTED:
                                String remissionDecisionReason =
                                        asylumCase.read(REMISSION_DECISION_REASON, String.class).orElse("");
                                remissionDetails.setRemissionDecision("Rejected");
                                remissionDetails.setRemissionDecisionReason(remissionDecisionReason);
                                break;

                            default:
                                break;
                        }
                    }
                }
            }
        );

        log.info("Setting temp previous remission details: " + tempPreviousRemissionDetails);
        asylumCase.write(TEMP_PREVIOUS_REMISSION_DETAILS, tempPreviousRemissionDetails);
    }
}
