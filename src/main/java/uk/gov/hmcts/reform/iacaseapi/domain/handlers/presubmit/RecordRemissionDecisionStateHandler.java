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
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class RecordRemissionDecisionStateHandler implements PreSubmitCallbackStateHandler<AsylumCase> {

    private final FeatureToggler featureToggler;
    private final DateProvider dateProvider;

    public RecordRemissionDecisionStateHandler(
        FeatureToggler featureToggler,
        DateProvider dateProvider
    ) {
        this.featureToggler = featureToggler;
        this.dateProvider = dateProvider;
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

        switch (remissionDecision) {
            case APPROVED:
                asylumCase.write(PAYMENT_STATUS, PaymentStatus.PAID);
                if (Arrays.asList(AppealType.EA, AppealType.HU).contains(appealType)) {
                    return new PreSubmitCallbackResponse<>(asylumCase, State.APPEAL_SUBMITTED);
                }
                return new PreSubmitCallbackResponse<>(asylumCase, currentState);

            case PARTIALLY_APPROVED:
            case REJECTED:
                asylumCase.write(PAYMENT_STATUS, PaymentStatus.PAYMENT_PENDING);
                asylumCase.write(REMISSION_REJECTED_DATE_PLUS_14DAYS,
                    LocalDate.parse(dateProvider.now().plusDays(14).toString()).format(DateTimeFormatter.ofPattern("d MMM yyyy")));
                return new PreSubmitCallbackResponse<>(asylumCase, currentState);

            default:
                return new PreSubmitCallbackResponse<>(asylumCase, currentState);
        }
    }
}
