package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Application;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ApplicationType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.PaymentStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class EndAppealHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;

    public EndAppealHandler(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.END_APPEAL || callback.getEvent() == Event.END_APPEAL_AUTOMATICALLY);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PaymentStatus paymentStatus = asylumCase.read(PAYMENT_STATUS, PaymentStatus.class)
            .orElse(PaymentStatus.PAYMENT_PENDING);
        if (callback.getEvent() == Event.END_APPEAL_AUTOMATICALLY && paymentStatus == PaymentStatus.PAID) {
            throw new IllegalStateException("Cannot auto end appeal as the payment is already made!");
        }

        asylumCase.write(END_APPEAL_DATE, dateProvider.now().toString());
        asylumCase.write(RECORD_APPLICATION_ACTION_DISABLED, YesOrNo.YES);

        asylumCase.clear(APPLICATION_WITHDRAW_EXISTS);
        asylumCase.clear(DISABLE_OVERVIEW_PAGE);
        asylumCase.clear(REINSTATE_APPEAL_REASON);
        asylumCase.clear(REINSTATED_DECISION_MAKER);
        asylumCase.clear(APPEAL_STATUS);
        asylumCase.clear(REINSTATE_APPEAL_DATE);

        changeWithdrawApplicationsToCompleted(asylumCase);

        State previousState = callback
            .getCaseDetailsBefore()
            .map(CaseDetails::getState)
            .orElseThrow(() -> new IllegalStateException("cannot find previous case state"));

        asylumCase.write(STATE_BEFORE_END_APPEAL, previousState);

        // Prevents data populated in MarkAsReadyForUtTransferHandler being displayed on UI
        asylumCase.clear(APPEAL_READY_FOR_UT_TRANSFER);
        asylumCase.clear(UT_APPEAL_REFERENCE_NUMBER);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void changeWithdrawApplicationsToCompleted(AsylumCase asylumCase) {
        asylumCase.write(APPLICATIONS, asylumCase.<List<IdValue<Application>>>read(APPLICATIONS)
            .orElse(emptyList())
            .stream()
            .map(application -> {
                String applicationType = application.getValue().getApplicationType();
                if (ApplicationType.WITHDRAW.toString().equals(applicationType)) {

                    return new IdValue<>(application.getId(), new Application(
                        application.getValue().getApplicationDocuments(),
                        application.getValue().getApplicationSupplier(),
                        applicationType,
                        application.getValue().getApplicationReason(),
                        application.getValue().getApplicationDate(),
                        application.getValue().getApplicationDecision(),
                        application.getValue().getApplicationDecisionReason(),
                        application.getValue().getApplicationDateOfDecision(),
                        "Completed"
                    ));
                }

                return application;
            })
            .collect(Collectors.toList())
        );
    }

}
