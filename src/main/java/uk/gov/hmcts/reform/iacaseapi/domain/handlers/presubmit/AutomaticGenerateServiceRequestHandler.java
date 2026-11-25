package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.NO_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

@Slf4j
@Component
public class AutomaticGenerateServiceRequestHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeePayment<AsylumCase> feePayment;
    private final boolean isFeePaymentEnabled;

    public AutomaticGenerateServiceRequestHandler(
        @Value("${featureFlag.isfeePaymentEnabled}") boolean isFeePaymentEnabled,
        FeePayment<AsylumCase> feePayment
    ) {
        this.isFeePaymentEnabled = isFeePaymentEnabled;
        this.feePayment = feePayment;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LAST;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Event event = callback.getEvent();

        if (callbackStage != PreSubmitCallbackStage.ABOUT_TO_SUBMIT || !isFeePaymentEnabled) {
            return false;
        }

        if (event == SUBMIT_APPEAL) {
            return canHandleSubmitAppeal(asylumCase);
        }

        if (event == RECORD_REMISSION_DECISION) {
            return canHandleRecordRemissionDecision(asylumCase);
        }

        return false;
    }

    private boolean canHandleSubmitAppeal(AsylumCase asylumCase) {
        Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);

        return isWaysToPay(asylumCase)
               && !HandlerUtils.isAipJourney(asylumCase)
               && !isInternalCase(asylumCase)
               && remissionType.isPresent()
               && remissionType.get() == NO_REMISSION;
    }

    private boolean canHandleRecordRemissionDecision(AsylumCase asylumCase) {
        Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

        if (remissionDecision.isEmpty()) {
            return false;
        }

        RemissionDecision decision = remissionDecision.get();
        boolean isRejectedOrPartiallyApproved = decision == RemissionDecision.REJECTED
                                                 || decision == RemissionDecision.PARTIALLY_APPROVED;

        return isRejectedOrPartiallyApproved && shouldCreateServiceRequest(asylumCase);
    }

    private boolean shouldCreateServiceRequest(AsylumCase asylumCase) {
        boolean isAppellantsRepresented = asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)
            .map(value -> value == YesOrNo.YES)
            .orElse(false);

        return !HandlerUtils.isAipJourney(asylumCase)
               && !isInternalCase(asylumCase)
               && !isAppellantsRepresented;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        // TODO: remove log
        log.debug("PreSubmit Callback to ia-case-payments-api to generate service request for case {}",
                callback.getCaseDetails().getId());
        AsylumCase asylumCase = feePayment.aboutToSubmit(callback);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isWaysToPay(AsylumCase asylumCase) {
        Optional<AppealType> optionalAppealType = asylumCase.read(APPEAL_TYPE, AppealType.class);
        if (optionalAppealType.isPresent()) {
            AppealType appealType = optionalAppealType.get();
            return List.of(AppealType.EA, AppealType.HU, AppealType.PA, AppealType.EU).contains(appealType);
        }
        return false;
    }
}
