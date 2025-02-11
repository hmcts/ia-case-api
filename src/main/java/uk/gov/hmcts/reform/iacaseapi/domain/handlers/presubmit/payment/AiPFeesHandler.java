package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.payment;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ASYLUM_SUPPORT_REF_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FEE_REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HELP_WITH_FEES_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HELP_WITH_FEES_REF_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LOCAL_AUTHORITY_LETTERS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption.WILL_PAY_FOR_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAipJourney;

@Component
public class AiPFeesHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeePayment<AsylumCase> feePayment;
    private final FeatureToggler featureToggler;

    public AiPFeesHandler(
        FeePayment<AsylumCase> feePayment,
        FeatureToggler featureToggler
    ) {
        this.feePayment = feePayment;
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && Arrays.asList(
                Event.START_APPEAL,
                Event.EDIT_APPEAL
        ).contains(callback.getEvent())
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
                asylumCase.clear(FEE_REMISSION_TYPE);
                setFeeRemissionTypeDetails(asylumCase);
                break;

            default:
                break;
        }

        return new PreSubmitCallbackResponse<>(feePayment.aboutToSubmit(callback));
    }

    private void setFeeRemissionTypeDetails(AsylumCase asylumCase) {

        Optional<RemissionOption> remissionOption = asylumCase.read(REMISSION_OPTION, RemissionOption.class);

        if (remissionOption.isPresent()) {
            switch (remissionOption.get()) {
                case ASYLUM_SUPPORT_FROM_HOME_OFFICE:
                    asylumCase.write(FEE_REMISSION_TYPE, "Asylum support");
                    Optional<String> asylumSupportRefNumber = asylumCase.read(ASYLUM_SUPPORT_REF_NUMBER, String.class);

                    if (asylumSupportRefNumber.isPresent()) {
                        asylumCase.clear(HELP_WITH_FEES_OPTION);
                        asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
                        asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
                    }
                    break;

                case FEE_WAIVER_FROM_HOME_OFFICE:
                    asylumCase.write(FEE_REMISSION_TYPE, "Home Office Waiver");
                    asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
                    asylumCase.clear(HELP_WITH_FEES_OPTION);
                    asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
                    asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
                    break;

                case UNDER_18_GET_SUPPORT:
                case PARENT_GET_SUPPORT:
                    asylumCase.write(FEE_REMISSION_TYPE, "Local Authority Support");
                    asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
                    asylumCase.clear(HELP_WITH_FEES_OPTION);
                    asylumCase.clear(HELP_WITH_FEES_REF_NUMBER);
                    break;

                case NO_REMISSION:
                case I_WANT_TO_GET_HELP_WITH_FEES:
                    Optional<HelpWithFeesOption> helpWithFeesOption = asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class);

                    if (helpWithFeesOption.isPresent() && helpWithFeesOption.get() != WILL_PAY_FOR_APPEAL) {
                        asylumCase.write(REMISSION_OPTION, "iWantToGetHelpWithFees");
                        asylumCase.write(FEE_REMISSION_TYPE, "Help with Fees");
                        asylumCase.clear(ASYLUM_SUPPORT_REF_NUMBER);
                        asylumCase.clear(LOCAL_AUTHORITY_LETTERS);
                    }
                    break;

                default:
                    break;
            }
        }

    }

}
