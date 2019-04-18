package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.time.LocalDate.parse;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class HomeOfficeDecisionDateChecker implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final int appealOutOfTimeDays;

    public HomeOfficeDecisionDateChecker(
        DateProvider dateProvider,
        @Value("${appealOutOfTimeDays}") int appealOutOfTimeDays
    ) {
        this.dateProvider = dateProvider;
        this.appealOutOfTimeDays = appealOutOfTimeDays;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START || callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
                && callback.getEvent() == Event.SUBMIT_APPEAL;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        LocalDate homeOfficeDecisionDate =
            parse(asylumCase.getHomeOfficeDecisionDate()
                .orElseThrow(() -> new RequiredFieldMissingException("homeOfficeDecisionDate is not present")));

        if (homeOfficeDecisionDate.isBefore(dateProvider.now().minusDays(appealOutOfTimeDays))) {
            asylumCase.setSubmissionOutOfTime(YES);
        } else {
            asylumCase.setSubmissionOutOfTime(NO);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
