package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
@Slf4j
public class RequestFeeRemissionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;

    public RequestFeeRemissionHandler(
        FeatureToggler featureToggler
    ) {
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.REQUEST_FEE_REMISSION
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

        setFeeRemissionTypeDetails(asylumCase);

        Optional<List<IdValue<RemissionDetails>>> previousRemissionDetailsOpt =
                asylumCase.read(TEMP_PREVIOUS_REMISSION_DETAILS);
        log.info("GETTING REMISSIONS: " + previousRemissionDetailsOpt.get());
        asylumCase.write(PREVIOUS_REMISSION_DETAILS, previousRemissionDetailsOpt.get());
        clearPreviousRemissionCaseFields(asylumCase);

        asylumCase.write(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void setFeeRemissionTypeDetails(AsylumCase asylumCase) {

        Optional<RemissionType> optRemissionType = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);
        String remissionClaim = asylumCase.read(REMISSION_CLAIM, String.class)
            .orElse("");

        if (optRemissionType.isPresent()) {

            if (optRemissionType.get() == RemissionType.HO_WAIVER_REMISSION) {

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
            } else if (optRemissionType.get() == RemissionType.HELP_WITH_FEES) {

                asylumCase.write(FEE_REMISSION_TYPE, "Help with Fees");
            } else if (optRemissionType.get() == RemissionType.EXCEPTIONAL_CIRCUMSTANCES_REMISSION) {

                asylumCase.write(FEE_REMISSION_TYPE, "Exceptional circumstances");
            }
        }
    }

    private void clearPreviousRemissionCaseFields(AsylumCase asylumCase) {

        final Optional<RemissionType> remissionType = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);
        String remissionClaim = asylumCase.read(REMISSION_CLAIM, String.class).orElse("");

        if (remissionType.isPresent()) {

            switch (remissionType.get()) {

                case HO_WAIVER_REMISSION:
                    if (remissionClaim.equals("asylumSupport")) {

                        clearLegalAidAccountNumberRemissionDetails(asylumCase);
                        clearSection17RemissionDetails(asylumCase);
                        clearSection20RemissionDetails(asylumCase);
                        clearHomeOfficeWaiverRemissionDetails(asylumCase);
                        clearHelpWithFeesRemissionDetails(asylumCase);
                        clearExceptionalCircumstancesRemissionDetails(asylumCase);
                    } else if (remissionClaim.equals("legalAid")) {

                        clearAsylumSupportRemissionDetails(asylumCase);
                        clearSection17RemissionDetails(asylumCase);
                        clearSection20RemissionDetails(asylumCase);
                        clearHomeOfficeWaiverRemissionDetails(asylumCase);
                        clearHelpWithFeesRemissionDetails(asylumCase);
                        clearExceptionalCircumstancesRemissionDetails(asylumCase);
                    } else if (remissionClaim.equals("section17")) {

                        clearAsylumSupportRemissionDetails(asylumCase);
                        clearLegalAidAccountNumberRemissionDetails(asylumCase);
                        clearSection20RemissionDetails(asylumCase);
                        clearHomeOfficeWaiverRemissionDetails(asylumCase);
                        clearHelpWithFeesRemissionDetails(asylumCase);
                        clearExceptionalCircumstancesRemissionDetails(asylumCase);
                    } else if (remissionClaim.equals("section20")) {

                        clearAsylumSupportRemissionDetails(asylumCase);
                        clearLegalAidAccountNumberRemissionDetails(asylumCase);
                        clearSection17RemissionDetails(asylumCase);
                        clearHomeOfficeWaiverRemissionDetails(asylumCase);
                        clearHelpWithFeesRemissionDetails(asylumCase);
                        clearExceptionalCircumstancesRemissionDetails(asylumCase);
                    } else if (remissionClaim.equals("homeOfficeWaiver")) {

                        clearAsylumSupportRemissionDetails(asylumCase);
                        clearLegalAidAccountNumberRemissionDetails(asylumCase);
                        clearSection17RemissionDetails(asylumCase);
                        clearSection20RemissionDetails(asylumCase);
                        clearHelpWithFeesRemissionDetails(asylumCase);
                        clearExceptionalCircumstancesRemissionDetails(asylumCase);
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

            asylumCase.clear(TEMP_PREVIOUS_REMISSION_DETAILS);
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

}
