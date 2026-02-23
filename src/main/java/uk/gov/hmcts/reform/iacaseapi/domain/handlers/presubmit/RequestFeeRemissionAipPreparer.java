package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.appealHasRemissionOptionOrType;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.clearRequestRemissionFields;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAipJourney;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
@Slf4j
public class RequestFeeRemissionAipPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;

    public RequestFeeRemissionAipPreparer(
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

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
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

        List<AppealType> allowedAppealTypes = List.of(AppealType.EA, AppealType.HU, AppealType.PA, AppealType.EU);
        if (!allowedAppealTypes.contains(appealType)) {
            return callbackResponse.withError("You cannot request a fee remission for this appeal");
        }

        Optional<RemissionOption> previousRemissionOption = asylumCase.read(REMISSION_OPTION, RemissionOption.class);
        Optional<HelpWithFeesOption> previousHelpWithFeesOptionAip = asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class);
        Optional<RemissionType> previousRemissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
        Optional<RemissionType> previousLateRemissionType = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);
        Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

        if (appealHasRemissionOptionOrType(previousRemissionOption, previousHelpWithFeesOptionAip, previousRemissionType, previousLateRemissionType)) {
            if (remissionDecision.isEmpty()) {
                return callbackResponse.withError("You cannot request a fee remission at this time because another fee remission request for this appeal has yet to be decided.");
            } else {
                asylumCase.write(HAS_PREVIOUS_REMISSION, YesOrNo.YES);
            }
        }

        clearRequestRemissionFields(asylumCase);
        return callbackResponse;
    }
}
