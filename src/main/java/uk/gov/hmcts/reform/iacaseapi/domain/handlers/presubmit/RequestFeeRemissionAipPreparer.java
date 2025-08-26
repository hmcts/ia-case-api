package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption.WILL_PAY_FOR_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.APPROVED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.PARTIALLY_APPROVED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.REJECTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.clearRequestRemissionFields;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAipJourney;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeRemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRoleLabel;
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
public class RequestFeeRemissionAipPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final RemissionDetailsAppender remissionDetailsAppender;
    private final DateProvider dateProvider;

    public RequestFeeRemissionAipPreparer(
        FeatureToggler featureToggler,
        RemissionDetailsAppender remissionDetailsAppender,
        DateProvider dateProvider
    ) {
        this.featureToggler = featureToggler;
        this.remissionDetailsAppender = remissionDetailsAppender;
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.REQUEST_FEE_REMISSION
            && isAipJourney(callback.getCaseDetails().getCaseData())
            && featureToggler.getValue("dlrm-refund-feature-flag", false);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
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
            case EU:
                Optional<RemissionOption> previousRemissionOption = asylumCase.read(REMISSION_OPTION, RemissionOption.class);
                Optional<RemissionType> previousRemissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
                Optional<HelpWithFeesOption> previousHelpWithFeesOptionAip = asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class);
                Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

                if (appealHasRemissionOption(previousRemissionOption, previousHelpWithFeesOptionAip, previousRemissionType)) {
                    if (remissionDecision.isEmpty()) {
                        callbackResponse.addError("You cannot request a fee remission at this time because another fee remission request for this appeal has yet to be decided.");
                        break;
                    } else if (List.of(APPROVED, PARTIALLY_APPROVED, REJECTED).contains(remissionDecision.get())) {
                        appendPreviousRemissionDetails(asylumCase);
                        asylumCase.write(PREVIOUS_REMISSION_DETAILS, remissionDetailsAppender.getRemissions());
                    }
                }
                asylumCase.write(IS_LATE_REMISSION_REQUEST, YesOrNo.YES);
                assignLateRemissionValuesToRemissionValues(asylumCase);
                clearPreviousAndLateRemissionFields(asylumCase);
                asylumCase.write(AsylumCaseFieldDefinition.REQUEST_FEE_REMISSION_DATE, dateProvider.now().toString());
                clearRequestRemissionFields(asylumCase);
                break;

            default:
                break;
        }

        return callbackResponse;
    }

    private boolean appealHasRemissionOption(Optional<RemissionOption> remissionOption, Optional<HelpWithFeesOption> helpWithFeesOption, Optional<RemissionType> remissionType) {
        return (remissionOption.isPresent() && remissionOption.get() != RemissionOption.NO_REMISSION)
            || (helpWithFeesOption.isPresent() && helpWithFeesOption.get() != WILL_PAY_FOR_APPEAL)
            || (remissionType.isPresent() && remissionType.get() != RemissionType.NO_REMISSION);
    }

    private void assignLateRemissionValuesToRemissionValues(AsylumCase asylumCase) {
        Optional<RemissionOption> lateRemissionOption = asylumCase.read(LATE_REMISSION_OPTION, RemissionOption.class);

        if (lateRemissionOption.isPresent()) {
            RemissionOption remissionOption = lateRemissionOption.get();
            asylumCase.write(REMISSION_OPTION, remissionOption);
            if (remissionOption == RemissionOption.FEE_WAIVER_FROM_HOME_OFFICE) {
                asylumCase.write(FEE_REMISSION_TYPE, FeeRemissionType.HO_WAIVER);
                asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
                asylumCase.clear(HELP_WITH_FEES_OPTION);
                asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
                asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
            } else if (remissionOption == RemissionOption.ASYLUM_SUPPORT_FROM_HOME_OFFICE) {
                asylumCase.write(FEE_REMISSION_TYPE, FeeRemissionType.ASYLUM_SUPPORT);
            } else if (remissionOption == RemissionOption.UNDER_18_GET_SUPPORT || remissionOption == RemissionOption.PARENT_GET_SUPPORT) {
                asylumCase.write(FEE_REMISSION_TYPE, FeeRemissionType.LOCAL_AUTHORITY_SUPPORT);
            }
        }

        Optional<String> lateAsylumSupportRefNumber = asylumCase.read(LATE_ASYLUM_SUPPORT_REF_NUMBER, String.class);

        if (lateAsylumSupportRefNumber.isPresent()) {
            asylumCase.write(ASYLUM_SUPPORT_REF_NUMBER, lateAsylumSupportRefNumber.get());

            asylumCase.clear(HELP_WITH_FEES_OPTION);
            asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
            asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
        }

        Optional<HelpWithFeesOption> lateHelpWithFees = asylumCase.read(LATE_HELP_WITH_FEES_OPTION, HelpWithFeesOption.class);
        Optional<String> lateHelpWithFeesRefNumber = asylumCase.read(LATE_HELP_WITH_FEES_REF_NUMBER, String.class);

        if (lateHelpWithFees.isPresent() && lateHelpWithFeesRefNumber.isPresent()) {
            asylumCase.write(FEE_REMISSION_TYPE, FeeRemissionType.HELP_WITH_FEES);
            asylumCase.write(HELP_WITH_FEES_OPTION, lateHelpWithFees.get());
            asylumCase.write(HELP_WITH_FEES_REF_NUMBER, lateHelpWithFeesRefNumber.get());

            asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
            asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
        }

        Optional<List<IdValue<DocumentWithMetadata>>> lateLocalAuthorityLetters = asylumCase.read(LATE_LOCAL_AUTHORITY_LETTERS);

        if (lateLocalAuthorityLetters.isPresent() && !lateLocalAuthorityLetters.get().isEmpty()) {
            asylumCase.write(LOCAL_AUTHORITY_LETTERS, lateLocalAuthorityLetters.get());

            asylumCase.clear(HELP_WITH_FEES_OPTION);
            asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
            asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
        }
    }

    private void appendPreviousRemissionDetails(AsylumCase asylumCase) {
        List<IdValue<RemissionDetails>> previousRemissionDetails = null;

        Optional<List<IdValue<RemissionDetails>>> maybeExistingRemissionDetails = asylumCase.read(PREVIOUS_REMISSION_DETAILS);
        final List<IdValue<RemissionDetails>> existingRemissionDetails = maybeExistingRemissionDetails.orElse(Collections.emptyList());

        UserRoleLabel previousRemissionRequestedBy = asylumCase.read(REMISSION_REQUESTED_BY, UserRoleLabel.class)
            .orElse(null);

        if (UserRoleLabel.CITIZEN.equals(previousRemissionRequestedBy)) {
            previousRemissionDetails = appendPreviousRemissionDetailsAppellant(asylumCase, previousRemissionDetails, existingRemissionDetails);
        } else {
            previousRemissionDetails = appendPreviousRemissionDetailsNonAppellant(asylumCase, previousRemissionDetails, existingRemissionDetails);
        }
        appendPreviousRemissionDecisionDetails(previousRemissionDetails, asylumCase);
    }

    private List<IdValue<RemissionDetails>> appendPreviousRemissionDetailsAppellant(AsylumCase asylumCase,
                                                                                    List<IdValue<RemissionDetails>> previousRemissionDetails,
                                                                                    List<IdValue<RemissionDetails>> existingRemissionDetails) {
        RemissionOption remissionOption = asylumCase.read(REMISSION_OPTION, RemissionOption.class)
            .orElseThrow(() -> new IllegalStateException("Previous fee remission type is not present"));
        switch (remissionOption) {
            case ASYLUM_SUPPORT_FROM_HOME_OFFICE:
                String asylumSupportReference = asylumCase.read(ASYLUM_SUPPORT_REF_NUMBER, String.class).orElse("");
                Document asylumSupportDocument = asylumCase.read(ASYLUM_SUPPORT_DOCUMENT, Document.class).orElse(null);

                previousRemissionDetails = remissionDetailsAppender.appendAsylumSupportRemissionDetails(
                    existingRemissionDetails, FeeRemissionType.ASYLUM_SUPPORT, asylumSupportReference, asylumSupportDocument);
                break;

            case FEE_WAIVER_FROM_HOME_OFFICE:
                Document homeOfficeWaiverDocument = asylumCase.read(HOME_OFFICE_WAIVER_DOCUMENT, Document.class)
                    .orElse(null);
                previousRemissionDetails = remissionDetailsAppender.appendHomeOfficeWaiverRemissionDetails(
                    existingRemissionDetails, FeeRemissionType.HO_WAIVER, homeOfficeWaiverDocument);
                break;

            case UNDER_18_GET_SUPPORT:
            case PARENT_GET_SUPPORT:
                Optional<List<IdValue<DocumentWithMetadata>>> localAuthorityLetters = asylumCase.read(LOCAL_AUTHORITY_LETTERS);
                previousRemissionDetails = remissionDetailsAppender.appendLocalAuthorityRemissionDetails(
                    existingRemissionDetails, FeeRemissionType.LOCAL_AUTHORITY_SUPPORT, localAuthorityLetters.orElse(null));
                break;

            case I_WANT_TO_GET_HELP_WITH_FEES:
                HelpWithFeesOption helpWithFeesOption = asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class).orElseThrow(() -> new IllegalStateException("Help with fees option is not present"));
                String helpWithFeesRefNumber = asylumCase.read(HELP_WITH_FEES_REF_NUMBER, String.class).orElse("");

                previousRemissionDetails = remissionDetailsAppender.appendRemissionOptionDetails(
                    existingRemissionDetails, FeeRemissionType.HELP_WITH_FEES, helpWithFeesOption.toString(), helpWithFeesRefNumber);
                break;

            default:
                break;
        }
        return previousRemissionDetails;
    }

    private List<IdValue<RemissionDetails>> appendPreviousRemissionDetailsNonAppellant(AsylumCase asylumCase,
                                                                                       List<IdValue<RemissionDetails>> previousRemissionDetails,
                                                                                       List<IdValue<RemissionDetails>> existingRemissionDetails) {
        RemissionType remissionType = asylumCase.read(FEE_REMISSION_TYPE, RemissionType.class)
            .orElse(null);
        String remissionClaim = asylumCase.read(REMISSION_CLAIM, String.class)
            .orElse("");
        if (remissionType == null) {
            return appendPreviousRemissionDetailsAppellant(asylumCase, previousRemissionDetails, existingRemissionDetails);
        }
        if (remissionType == RemissionType.HO_WAIVER_REMISSION) {
            switch (remissionClaim) {
                case "asylumSupport":
                    String asylumSupportReference = asylumCase.read(ASYLUM_SUPPORT_REFERENCE, String.class).orElse("");
                    Document asylumSupportDocument = asylumCase.read(ASYLUM_SUPPORT_DOCUMENT, Document.class).orElse(null);
                    previousRemissionDetails = remissionDetailsAppender.appendAsylumSupportRemissionDetails(
                        existingRemissionDetails, FeeRemissionType.ASYLUM_SUPPORT, asylumSupportReference, asylumSupportDocument);
                    break;

                case "legalAid":
                    String legalAidNumber = asylumCase.read(LEGAL_AID_ACCOUNT_NUMBER, String.class).orElse("");
                    previousRemissionDetails = remissionDetailsAppender.appendLegalAidRemissionDetails(
                        existingRemissionDetails, FeeRemissionType.LEGAL_AID, legalAidNumber);
                    break;

                case "section17":
                    Optional<Document> section17Document = asylumCase.read(SECTION17_DOCUMENT, Document.class);
                    previousRemissionDetails = remissionDetailsAppender.appendSection17RemissionDetails(
                        existingRemissionDetails, FeeRemissionType.SECTION_17, section17Document.orElse(null));
                    break;

                case "section20":
                    Optional<Document> section20Document = asylumCase.read(SECTION20_DOCUMENT, Document.class);
                    previousRemissionDetails = remissionDetailsAppender.appendSection20RemissionDetails(
                        existingRemissionDetails, FeeRemissionType.SECTION_20, section20Document.orElse(null));
                    break;

                case "homeOfficeWaiver":
                    Document homeOfficeWaiverDocument = asylumCase.read(HOME_OFFICE_WAIVER_DOCUMENT, Document.class)
                        .orElse(null);
                    previousRemissionDetails = remissionDetailsAppender.appendHomeOfficeWaiverRemissionDetails(
                        existingRemissionDetails, FeeRemissionType.HO_WAIVER, homeOfficeWaiverDocument);
                    break;

                default:
                    break;
            }
        } else if (remissionType == RemissionType.HELP_WITH_FEES) {
            String helpWithReference =
                asylumCase.read(HELP_WITH_FEES_REFERENCE_NUMBER, String.class).orElse("");
            previousRemissionDetails = remissionDetailsAppender.appendHelpWithFeeReferenceRemissionDetails(
                existingRemissionDetails, FeeRemissionType.HELP_WITH_FEES, helpWithReference);
        } else if (remissionType == RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION) {
            String exceptionalCircumstances = asylumCase.read(EXCEPTIONAL_CIRCUMSTANCES, String.class)
                .orElseThrow(() -> new IllegalStateException("Exceptional circumstances details not present"));
            Optional<List<IdValue<Document>>> exceptionalCircumstancesDocuments =
                asylumCase.read(REMISSION_EC_EVIDENCE_DOCUMENTS);
            previousRemissionDetails = remissionDetailsAppender.appendExceptionalCircumstancesRemissionDetails(
                existingRemissionDetails, FeeRemissionType.EXCEPTIONAL_CIRCUMSTANCES, exceptionalCircumstances,
                exceptionalCircumstancesDocuments.orElse(null)
            );
        }
        return previousRemissionDetails;
    }


    private void appendPreviousRemissionDecisionDetails(List<IdValue<RemissionDetails>> previousRemissionDetails, AsylumCase asylumCase) {
        RemissionDecision remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
            .orElseThrow(() -> new IllegalStateException("Remission decision is not present"));
        String feeAmount = asylumCase.read(FEE_AMOUNT_GBP, String.class).orElse("");

        previousRemissionDetails
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

    private void clearPreviousAndLateRemissionFields(AsylumCase asylumCase) {
        asylumCase.clear(LATE_REMISSION_OPTION);
        asylumCase.clear(LATE_ASYLUM_SUPPORT_REF_NUMBER);
        asylumCase.clear(LATE_HELP_WITH_FEES_OPTION);
        asylumCase.clear(LATE_HELP_WITH_FEES_REF_NUMBER);
        asylumCase.clear(LATE_LOCAL_AUTHORITY_LETTERS);

        asylumCase.clear(REMISSION_DECISION);
        asylumCase.clear(AMOUNT_REMITTED);
        asylumCase.clear(AMOUNT_LEFT_TO_PAY);
        asylumCase.clear(REMISSION_DECISION_REASON);
    }

}
