package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AMOUNT_LEFT_TO_PAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AMOUNT_REMITTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ASYLUM_SUPPORT_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ASYLUM_SUPPORT_REFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ASYLUM_SUPPORT_REF_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EXCEPTIONAL_CIRCUMSTANCES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_AMOUNT_GBP;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_PREVIOUS_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HELP_WITH_FEES_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HELP_WITH_FEES_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HELP_WITH_FEES_REF_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_WAIVER_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_LATE_REMISSION_REQUEST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LATE_ASYLUM_SUPPORT_REF_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LATE_HELP_WITH_FEES_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LATE_HELP_WITH_FEES_REF_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LATE_LOCAL_AUTHORITY_LETTERS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LATE_REMISSION_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_AID_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LOCAL_AUTHORITY_LETTERS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREVIOUS_REMISSION_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_CLAIM;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_DECISION_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_EC_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_REQUESTED_BY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SECTION17_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SECTION20_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.APPROVED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.PARTIALLY_APPROVED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.REJECTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.clearPreviousRemissionCaseFields;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAipJourney;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.setFeeRemissionTypeDetails;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsHelper;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeRemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
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
@Slf4j
public class RequestFeeRemissionAipHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final RemissionDetailsAppender remissionDetailsAppender;
    private final DateProvider dateProvider;
    private final UserDetails userDetails;
    private final UserDetailsHelper userDetailsHelper;

    public RequestFeeRemissionAipHandler(
        FeatureToggler featureToggler,
        RemissionDetailsAppender remissionDetailsAppender,
        DateProvider dateProvider,
        UserDetails userDetails,
        UserDetailsHelper userDetailsHelper
    ) {
        this.featureToggler = featureToggler;
        this.remissionDetailsAppender = remissionDetailsAppender;
        this.dateProvider = dateProvider;
        this.userDetails = userDetails;
        this.userDetailsHelper = userDetailsHelper;
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

        boolean hasPreviousRemission = asylumCase.read(HAS_PREVIOUS_REMISSION, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES);
        RemissionDecision remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class).orElse(null);

        if (hasPreviousRemission && remissionDecision != null) {
            appendPreviousRemissionDetails(asylumCase);
            asylumCase.write(PREVIOUS_REMISSION_DETAILS, remissionDetailsAppender.getRemissions());
        }
        asylumCase.write(IS_LATE_REMISSION_REQUEST, YesOrNo.YES);
        UserRoleLabel currentUser = userDetailsHelper.getLoggedInUserRoleLabel(userDetails);
        if (currentUser == UserRoleLabel.CITIZEN) {
            setFeeRemissionOptionDetails(asylumCase);
        } else {
            setFeeRemissionTypeDetails(asylumCase);
        }
        clearPreviousAndLateRemissionFields(asylumCase);
        clearPreviousRemissionCaseFields(asylumCase);
        asylumCase.write(AsylumCaseFieldDefinition.REQUEST_FEE_REMISSION_DATE, dateProvider.now().toString());
        asylumCase.write(REMISSION_REQUESTED_BY, currentUser);
        return callbackResponse;
    }

    private void setFeeRemissionOptionDetails(AsylumCase asylumCase) {
        RemissionOption remissionOption = asylumCase.read(LATE_REMISSION_OPTION, RemissionOption.class)
            .orElse(RemissionOption.NO_REMISSION);
        if (remissionOption.equals(RemissionOption.NO_REMISSION)) {
            return;
        }
        asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
        asylumCase.clear(HELP_WITH_FEES_OPTION);
        asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
        asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
        asylumCase.write(REMISSION_OPTION, remissionOption);
        switch (remissionOption) {
            case FEE_WAIVER_FROM_HOME_OFFICE -> asylumCase.write(FEE_REMISSION_TYPE, FeeRemissionType.HO_WAIVER);
            case ASYLUM_SUPPORT_FROM_HOME_OFFICE -> setAsylumSupportRef(asylumCase);
            case PARENT_GET_SUPPORT, UNDER_18_GET_SUPPORT -> setLocalAuthorityLetters(asylumCase);
            case I_WANT_TO_GET_HELP_WITH_FEES -> setHelpWithFees(asylumCase);
            default -> log.info("Remission option is invalid, no fee remission will be set");
        }
    }

    private void setAsylumSupportRef(AsylumCase asylumCase) {
        asylumCase.write(FEE_REMISSION_TYPE, FeeRemissionType.ASYLUM_SUPPORT);
        asylumCase.read(LATE_ASYLUM_SUPPORT_REF_NUMBER, String.class)
            .ifPresent((asylumCaseRef) -> asylumCase.write(ASYLUM_SUPPORT_REF_NUMBER, asylumCaseRef));
    }

    private void setLocalAuthorityLetters(AsylumCase asylumCase) {
        asylumCase.write(FEE_REMISSION_TYPE, FeeRemissionType.LOCAL_AUTHORITY_SUPPORT);
        asylumCase.read(LATE_LOCAL_AUTHORITY_LETTERS, List.class)
            .filter(lateAuthorityLetters -> !lateAuthorityLetters.isEmpty())
            .ifPresent(lateAuthorityLetters -> asylumCase.write(LOCAL_AUTHORITY_LETTERS, lateAuthorityLetters));
    }

    private void setHelpWithFees(AsylumCase asylumCase) {
        asylumCase.write(FEE_REMISSION_TYPE, FeeRemissionType.HELP_WITH_FEES);
        asylumCase.read(LATE_HELP_WITH_FEES_OPTION, HelpWithFeesOption.class).ifPresent(option ->
            asylumCase.read(LATE_HELP_WITH_FEES_REF_NUMBER, String.class).ifPresent(ref -> {
                asylumCase.write(HELP_WITH_FEES_OPTION, option);
                asylumCase.write(HELP_WITH_FEES_REF_NUMBER, ref);
            }));
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
        RemissionType remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class)
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
