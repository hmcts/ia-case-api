package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.RemissionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeePayment;

@Component
public class RecordRemissionDecisionStateHandler implements PreSubmitCallbackStateHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final DateProvider dateProvider;
    private final FeePayment<AsylumCase> feePayment;

    public RecordRemissionDecisionStateHandler(
        FeatureToggler featureToggler,
        DateProvider dateProvider,
        FeePayment<AsylumCase> feePayment
    ) {
        this.featureToggler = featureToggler;
        this.dateProvider = dateProvider;
        this.feePayment = feePayment;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.RECORD_REMISSION_DECISION
               && featureToggler.getValue("remissions-feature", false);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback,
        PreSubmitCallbackResponse<AsylumCase> callbackResponse
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        final State currentState =
            callback
                .getCaseDetails()
                .getState();

        final AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class)
            .orElseThrow(() -> new IllegalStateException("Appeal type is not present"));

        final RemissionDecision remissionDecision = asylumCase.read(REMISSION_DECISION, RemissionDecision.class)
            .orElseThrow(() -> new IllegalStateException("Remission decision is not present"));

        final PaymentStatus paymentStatus = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class).orElse(PaymentStatus.PAYMENT_PENDING);

        switch (remissionDecision) {
            case APPROVED:
                asylumCase.write(PAYMENT_STATUS, PaymentStatus.PAID);
                asylumCase.write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.NO);
                asylumCase.write(DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION, YesOrNo.NO);

                if (Arrays.asList(AppealType.EA, AppealType.HU, AppealType.EU, AppealType.AG).contains(appealType)) {
                    return new PreSubmitCallbackResponse<>(asylumCase, State.APPEAL_SUBMITTED);
                }
                return new PreSubmitCallbackResponse<>(asylumCase, currentState);

            case PARTIALLY_APPROVED:
                asylumCase.write(PAYMENT_STATUS, paymentStatus);
                asylumCase.write(REMISSION_REJECTED_DATE_PLUS_14DAYS,
                    LocalDate.parse(dateProvider.now().plusDays(14).toString()).format(DateTimeFormatter.ofPattern("d MMM yyyy")));

                asylumCase.write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.NO);
                asylumCase.write(DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION, YesOrNo.YES);

                return new PreSubmitCallbackResponse<>(asylumCase, currentState);

            case REJECTED:
                asylumCase.write(PAYMENT_STATUS, paymentStatus);
                asylumCase.write(REMISSION_REJECTED_DATE_PLUS_14DAYS,
                    LocalDate.parse(dateProvider.now().plusDays(14).toString()).format(DateTimeFormatter.ofPattern("d MMM yyyy")));

                feePayment.aboutToSubmit(callback);

                asylumCase.write(IS_SERVICE_REQUEST_TAB_VISIBLE_CONSIDERING_REMISSIONS, YesOrNo.YES);
                asylumCase.write(REQUEST_FEE_REMISSION_FLAG_FOR_SERVICE_REQUEST, YesOrNo.NO);
                asylumCase.write(DISPLAY_MARK_AS_PAID_EVENT_FOR_PARTIAL_REMISSION, YesOrNo.NO);

                return new PreSubmitCallbackResponse<>(asylumCase, currentState);

            default:
                return new PreSubmitCallbackResponse<>(asylumCase, currentState);
        }
    }
}
