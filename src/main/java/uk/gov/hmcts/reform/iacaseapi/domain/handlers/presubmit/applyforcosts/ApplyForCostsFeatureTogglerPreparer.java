package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.applyforcosts;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.APPLY_FOR_COSTS;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class ApplyForCostsFeatureTogglerPreparer implements PreSubmitCallbackHandler<AsylumCase> {
    private final FeatureToggler featureToggler;
    private final int applicationOutOfTimeDays;
    private final DateProvider dateProvider;

    public ApplyForCostsFeatureTogglerPreparer(
            FeatureToggler featureToggler,
            @Value("${appealOutOfTimeDaysOoc}") int applicationOutOfTimeDays,
            DateProvider dateProvider) {
        this.featureToggler = featureToggler;
        this.applicationOutOfTimeDays = applicationOutOfTimeDays;
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START && (callback.getEvent() == APPLY_FOR_COSTS);
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        boolean isApplyForCostsFeatureEnabled
                = featureToggler.getValue("apply-for-costs-feature", false);

        asylumCase.write(IS_APPLY_FOR_COSTS_OOT, applyForCostsOot(asylumCase));

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        if (!isApplyForCostsFeatureEnabled) {
            response.addError("You cannot currently use this service to apply for costs");
        }

        return response;
    }

    private YesOrNo applyForCostsOot(AsylumCase asylumCase) {
        String endAppealDate = asylumCase.read(END_APPEAL_DATE, String.class).orElse("");
        String sendDecisionAndReasonsDate = asylumCase.read(SEND_DECISIONS_AND_REASONS_DATE, String.class).orElse("");
        LocalDate applyForCostsDate = dateProvider.now();

        if (!endAppealDate.isEmpty()) {
            return LocalDate.parse(endAppealDate).plusDays(applicationOutOfTimeDays).isBefore(applyForCostsDate)
                    ? YesOrNo.YES
                    : YesOrNo.NO;
        } else if (!sendDecisionAndReasonsDate.isEmpty()) {
            return LocalDate.parse(sendDecisionAndReasonsDate).plusDays(applicationOutOfTimeDays).isBefore(applyForCostsDate)
                    ? YesOrNo.YES
                    : YesOrNo.NO;
        }

        return YesOrNo.NO;
    }
}
