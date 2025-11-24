package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.clearRequestRemissionFields;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAipJourney;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
@Slf4j
public class RequestFeeRemissionPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;

    public RequestFeeRemissionPreparer(
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
                Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);
                Optional<RemissionType> lateRemissionType = asylumCase.read(LATE_REMISSION_TYPE, RemissionType.class);
                Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

                if ((remissionType.isPresent() && remissionType.get() != RemissionType.NO_REMISSION && remissionDecision.isEmpty())
                        || (lateRemissionType.isPresent() && remissionDecision.isEmpty())) {
                    callbackResponse
                        .addError("You cannot request a fee remission at this time because another fee remission request for this appeal "
                                  + "has yet to be decided.");
                } else if (isPreviousRemissionExists(remissionType, remissionDecision)
                        || isPreviousRemissionExists(lateRemissionType, remissionDecision)) {
                    if (asylumCase.read(FEE_REMISSION_TYPE, String.class).isEmpty()) {
                        throw(new IllegalStateException("Previous fee remission type is not present"));
                    }
                    clearRequestRemissionFields(asylumCase);
                }
                break;

            default:
                break;
        }

        return callbackResponse;
    }

    private boolean isPreviousRemissionExists(
            Optional<RemissionType> remissionType,
            Optional<RemissionDecision> remissionDecision
    ) {
        return remissionType.isPresent()
               && remissionType.get() != RemissionType.NO_REMISSION
               && remissionDecision.isPresent()
               && asList(APPROVED, PARTIALLY_APPROVED, REJECTED)
                   .contains(remissionDecision.get());
    }
}
