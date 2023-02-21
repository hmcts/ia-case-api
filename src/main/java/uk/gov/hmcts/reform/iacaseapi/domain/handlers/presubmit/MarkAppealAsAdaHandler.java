package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MARK_APPEAL_AS_ADA;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class MarkAppealAsAdaHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;

    public MarkAppealAsAdaHandler(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == MARK_APPEAL_AS_ADA;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
                callback
                        .getCaseDetails()
                        .getCaseData();

        if (HandlerUtils.isAppellantInDetention(asylumCase)
            && !(HandlerUtils.isAcceleratedDetainedAppeal(asylumCase) || HandlerUtils.isAgeAssessmentAppeal(asylumCase))) {

            String markAppealAsAdaReason = asylumCase.read(MARK_APPEAL_AS_ADA_EXPLANATION, String.class).orElse(null);

            requireNonNull(markAppealAsAdaReason, "Explain why this appeal is being marked an accelerated detained appeal is required");

            asylumCase.write(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.YES);
            asylumCase.write(DETENTION_STATUS, DetentionStatus.ACCELERATED);
            asylumCase.write(DATE_MARKED_AS_ADA, dateProvider.now().toString());
            asylumCase.write(REASON_APPEAL_MARKED_AS_ADA, markAppealAsAdaReason);
            asylumCase.write(ADA_SUFFIX, HandlerUtils.getAdaSuffix());

            if (asylumCase.read(DECISION_LETTER_RECEIVED_DATE).isEmpty()) {
                asylumCase.write(DECISION_LETTER_RECEIVED_DATE, asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class).orElse(null));
                asylumCase.clear(HOME_OFFICE_DECISION_DATE);
            }

            asylumCase.clear(MARK_APPEAL_AS_ADA_EXPLANATION);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);

    }

}
