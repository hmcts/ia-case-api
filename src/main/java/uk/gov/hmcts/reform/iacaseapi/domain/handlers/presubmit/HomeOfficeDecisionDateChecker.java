package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.time.LocalDate.parse;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.No;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.Yes;

import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class HomeOfficeDecisionDateChecker implements PreSubmitCallbackHandler<CaseDataMap> {

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
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START || callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT)
                && callback.getEvent() == Event.SUBMIT_APPEAL;
    }

    public PreSubmitCallbackResponse<CaseDataMap> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final CaseDataMap CaseDataMap =
            callback
                .getCaseDetails()
                .getCaseData();

        LocalDate homeOfficeDecisionDate =
            parse(CaseDataMap.getHomeOfficeDecisionDate()
                .orElseThrow(() -> new RequiredFieldMissingException("homeOfficeDecisionDate is not present")));

        if (homeOfficeDecisionDate.isBefore(dateProvider.now().minusDays(appealOutOfTimeDays))) {
            CaseDataMap.setSubmissionOutOfTime(Yes);
        } else {
            CaseDataMap.setSubmissionOutOfTime(No);
        }

        return new PreSubmitCallbackResponse<>(CaseDataMap);
    }
}
