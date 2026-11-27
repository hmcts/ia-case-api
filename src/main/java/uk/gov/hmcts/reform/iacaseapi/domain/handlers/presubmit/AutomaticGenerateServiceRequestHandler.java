package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HELP_WITH_FEES_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMISSION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionType.NO_REMISSION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_REMISSION_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAipJourney;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isInternalCase;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HelpWithFeesOption;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionOption;
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

        log.info("PreSubmit Callback for event: {}, caseID: {}", event, callback.getCaseDetails().getId());
        if (callbackStage != PreSubmitCallbackStage.ABOUT_TO_SUBMIT || !isFeePaymentEnabled) {
            return false;
        }

        if (event == SUBMIT_APPEAL) {
            boolean canHandleLegalRep = canHandleSubmitAppeal(asylumCase);
            boolean canHandleAiP = canHandleSubmitAppealAip(asylumCase);
            log.info("SubmitAppeal caseID: {}, journey: {}, AiP remission: {}, LR: {}, AiP: {}",
                    callback.getCaseDetails().getId(), asylumCase.read(JOURNEY_TYPE, RemissionOption.class),
                    asylumCase.read(REMISSION_OPTION, RemissionOption.class),
                    canHandleLegalRep, canHandleAiP);
            return canHandleLegalRep || canHandleAiP;
        }

        if (event == RECORD_REMISSION_DECISION) {
            boolean canHandle = canHandleRecordRemissionDecision(asylumCase)
                || canHandleRecordRemissionDecisionAip(asylumCase);
            log.info("PreSubmit Callback for event: {}, canHandle? {}, caseID: {}", event,
                    canHandle, callback.getCaseDetails().getId());
            return canHandle;
        }

        return false;
    }

    private boolean canHandleSubmitAppeal(AsylumCase asylumCase) {
        Optional<RemissionType> remissionType = asylumCase.read(REMISSION_TYPE, RemissionType.class);

        return isEaHuPaEu(asylumCase)
               && !isAipJourney(asylumCase)
               && !isInternalCase(asylumCase)
               && remissionType.isPresent()
               && remissionType.get() == NO_REMISSION;
    }

    private boolean canHandleSubmitAppealAip(AsylumCase asylumCase) {
        Optional<RemissionOption> remissionOption = asylumCase.read(REMISSION_OPTION, RemissionOption.class);
        Optional<HelpWithFeesOption> helpWithFeesOption = asylumCase.read(HELP_WITH_FEES_OPTION, HelpWithFeesOption.class);

        boolean noRemissionAndWillPay = remissionOption.isPresent()
            && remissionOption.get() == RemissionOption.NO_REMISSION
            && helpWithFeesOption.isPresent()
            && helpWithFeesOption.get() == HelpWithFeesOption.WILL_PAY_FOR_APPEAL;

        return isEaHuPaEu(asylumCase)
               && isAipJourney(asylumCase)
               && !isInternalCase(asylumCase)
               && noRemissionAndWillPay;
    }

    private boolean canHandleRecordRemissionDecision(AsylumCase asylumCase) {
        Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

        boolean isAppellantsRepresented = asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)
            .map(value -> value == YesOrNo.YES)
            .orElse(false);

        log.info("RemissionDecision: {}, isAipJourney? {}, isInternalCase: {}, isAppellantsRepresented: {}",
                remissionDecision, isAipJourney(asylumCase), isInternalCase(asylumCase), isAppellantsRepresented);

        if (remissionDecision.isEmpty()) {
            return false;
        }

        RemissionDecision decision = remissionDecision.get();
        boolean isRejectedOrPartiallyApproved = decision == RemissionDecision.REJECTED
                                                 || decision == RemissionDecision.PARTIALLY_APPROVED;

        return isRejectedOrPartiallyApproved
               && isEaHuPaEu(asylumCase)
               && !isAipJourney(asylumCase)
               && !isInternalCase(asylumCase)
               && !isAppellantsRepresented;
    }

    private boolean canHandleRecordRemissionDecisionAip(AsylumCase asylumCase) {
        Optional<RemissionDecision> remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class);

        if (remissionDecision.isEmpty()) {
            return false;
        }

        RemissionDecision decision = remissionDecision.get();
        boolean isRejectedOrPartiallyApproved = decision == RemissionDecision.REJECTED
                                                 || decision == RemissionDecision.PARTIALLY_APPROVED;

        return isRejectedOrPartiallyApproved
               && isEaHuPaEu(asylumCase)
               && isAipJourney(asylumCase)
               && !isInternalCase(asylumCase);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            log.info("Can not handle callbackStage: {}, Id: {}", callbackStage, callback.getCaseDetails().getId());
            throw new IllegalStateException("Cannot handle callback");
        }

        // TODO: remove log
        log.info("PreSubmit Callback to ia-case-payments-api to generate service request for case {}",
                callback.getCaseDetails().getId());
        AsylumCase asylumCase = feePayment.aboutToSubmit(callback);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isEaHuPaEu(AsylumCase asylumCase) {
        Optional<AppealType> optionalAppealType = asylumCase.read(APPEAL_TYPE, AppealType.class);
        if (optionalAppealType.isPresent()) {
            AppealType appealType = optionalAppealType.get();
            return List.of(AppealType.EA, AppealType.HU, AppealType.PA, AppealType.EU).contains(appealType);
        }
        return false;
    }
}
