package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FeeRemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.RemissionDetailsAppender;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.EU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.HU;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType.PA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ASYLUM_SUPPORT_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ASYLUM_SUPPORT_REFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EXCEPTIONAL_CIRCUMSTANCES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HELP_WITH_FEES_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_WAIVER_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_AID_ACCOUNT_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_EC_EVIDENCE_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SECTION17_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SECTION20_DOCUMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.TEMP_PREVIOUS_REMISSION_DETAILS;

@Component
@Slf4j
public class SubmitAppealHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final RemissionDetailsAppender remissionDetailsAppender;

    public SubmitAppealHandler(
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
               && callback.getEvent() == Event.SUBMIT_APPEAL
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

        Optional<String> feeRemissionTypeOpt = asylumCase.read(FEE_REMISSION_TYPE, String.class);

        log.info("SubmitAppeal feeRemissionTypeOpt: " + feeRemissionTypeOpt);
        if (feeRemissionTypeOpt.isPresent()) {
            log.info("SubmitAppeal appealType: " + appealType);
            if (appealType == EA || appealType == HU || appealType == PA || appealType == EU) {
                appendTempPreviousRemissionDetails(asylumCase, feeRemissionTypeOpt.get());
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void appendTempPreviousRemissionDetails(AsylumCase asylumCase, String feeRemissionType) {
        List<IdValue<RemissionDetails>> tempPreviousRemissionDetails = null;

        log.info("SubmitAppeal feeRemissionType: " + feeRemissionType);
        switch (feeRemissionType) {
            case FeeRemissionType.ASYLUM_SUPPORT:
                String asylumSupportReference = asylumCase.read(ASYLUM_SUPPORT_REFERENCE, String.class)
                        .orElse("");
                Optional<Document>  asylumSupportDocument = asylumCase.read(ASYLUM_SUPPORT_DOCUMENT);

                tempPreviousRemissionDetails =
                    remissionDetailsAppender.appendAsylumSupportRemissionDetails(
                        emptyList(),
                        feeRemissionType,
                        asylumSupportReference,
                        asylumSupportDocument.orElse(null)
                    );
                break;

            case FeeRemissionType.LEGAL_AID:
                String legalAidAccountNumber = asylumCase.read(LEGAL_AID_ACCOUNT_NUMBER, String.class)
                        .orElse("");

                tempPreviousRemissionDetails =
                    remissionDetailsAppender.appendLegalAidRemissionDetails(
                        emptyList(),
                        feeRemissionType,
                        legalAidAccountNumber
                    );
                break;

            case FeeRemissionType.SECTION_17:
                Optional<Document> section17Document = asylumCase.read(SECTION17_DOCUMENT);

                if (section17Document.isPresent()) {
                    tempPreviousRemissionDetails =
                        remissionDetailsAppender.appendSection17RemissionDetails(
                            emptyList(),
                            feeRemissionType,
                            section17Document.get()
                        );
                }
                break;

            case FeeRemissionType.SECTION_20:
                Optional<Document> section20Document = asylumCase.read(SECTION20_DOCUMENT);

                if (section20Document.isPresent()) {
                    tempPreviousRemissionDetails =
                        remissionDetailsAppender.appendSection20RemissionDetails(
                            emptyList(),
                            feeRemissionType,
                            section20Document.get()
                        );
                }
                break;

            case FeeRemissionType.HO_WAIVER:
                Optional<Document> homeOfficeWaiverDocument = asylumCase.read(HOME_OFFICE_WAIVER_DOCUMENT);

                if (homeOfficeWaiverDocument.isPresent()) {
                    tempPreviousRemissionDetails =
                        remissionDetailsAppender.appendHomeOfficeWaiverRemissionDetails(
                            emptyList(),
                            feeRemissionType,
                            homeOfficeWaiverDocument.get()
                        );
                }
                break;

            case FeeRemissionType.HELP_WITH_FEES:
                String helpWithReference = asylumCase.read(HELP_WITH_FEES_REFERENCE_NUMBER, String.class)
                        .orElse("");

                tempPreviousRemissionDetails =
                    remissionDetailsAppender.appendHelpWithFeeReferenceRemissionDetails(
                        emptyList(),
                        feeRemissionType,
                        helpWithReference
                    );
                break;

            case FeeRemissionType.EXCEPTIONAL_CIRCUMSTANCES:
                String exceptionalCircumstances = asylumCase.read(EXCEPTIONAL_CIRCUMSTANCES, String.class)
                        .orElseThrow(() -> new IllegalStateException("Exceptional circumstances details not present"));
                Optional<List<IdValue<Document>>> exceptionalCircumstancesDocuments =
                        asylumCase.read(REMISSION_EC_EVIDENCE_DOCUMENTS);

                tempPreviousRemissionDetails =
                    remissionDetailsAppender.appendExceptionalCircumstancesRemissionDetails(
                        emptyList(),
                        feeRemissionType,
                        exceptionalCircumstances,
                        exceptionalCircumstancesDocuments.orElse(null)
                    );
                break;

            default:
                break;
        }

        log.info("Setting temp previous remission details: " + tempPreviousRemissionDetails);
        if (tempPreviousRemissionDetails != null) {
            asylumCase.write(TEMP_PREVIOUS_REMISSION_DETAILS, tempPreviousRemissionDetails);
        }
    }
}
