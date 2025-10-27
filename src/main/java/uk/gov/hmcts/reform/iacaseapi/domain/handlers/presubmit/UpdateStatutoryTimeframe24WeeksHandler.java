package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StatutoryTimeframe24Weeks;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

@Component
public class UpdateStatutoryTimeframe24WeeksHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final Appender<StatutoryTimeframe24Weeks> statutoryTimeframe24WeeksAppender;
    private final DateProvider dateProvider;
    private final UserDetails userDetails;

    public UpdateStatutoryTimeframe24WeeksHandler(
        Appender<StatutoryTimeframe24Weeks> statutoryTimeframe24WeeksAppender,
        DateProvider dateProvider,
        UserDetails userDetails
    ) {
        this.statutoryTimeframe24WeeksAppender = statutoryTimeframe24WeeksAppender;
        this.dateProvider = dateProvider;
        this.userDetails = userDetails;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_SUBMIT) && callback.getEvent().equals(UPDATE_STATUTORY_TIMEFRAME_24_WEEKS);
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

        YesOrNo fasterCaseStatus = asylumCase
                .read(STATUTORY_TIMEFRAME_24_WEEKS_STATUS, YesOrNo.class)
                .orElseThrow(() -> new IllegalStateException("statutoryTimeframe24WeeksStatus is not present"));

        String fasterCaseStatusReason = asylumCase
                .read(STATUTORY_TIMEFRAME_24_WEEKS_REASON, String.class)
                .orElseThrow(() -> new IllegalStateException("statutoryTimeframe24WeeksReason is not present"));

        Optional<List<IdValue<StatutoryTimeframe24Weeks>>> maybeExistingStatutoryTimeframe24Weeks =
            asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS);

        final StatutoryTimeframe24Weeks newStatutoryTimeframe24Weeks = new StatutoryTimeframe24Weeks(
            fasterCaseStatus,
            fasterCaseStatusReason,
            buildFullName(),
            dateProvider.now().toString()
        );

        List<IdValue<StatutoryTimeframe24Weeks>> allStatutoryTimeframe24Weeks =
            statutoryTimeframe24WeeksAppender.append(newStatutoryTimeframe24Weeks, maybeExistingStatutoryTimeframe24Weeks.orElse(emptyList()));

        asylumCase.write(STATUTORY_TIMEFRAME_24_WEEKS, allStatutoryTimeframe24Weeks);

        asylumCase.clear(STATUTORY_TIMEFRAME_24_WEEKS_STATUS);
        asylumCase.clear(STATUTORY_TIMEFRAME_24_WEEKS_REASON);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String buildFullName() {
        return userDetails.getForename()
            + " "
            + userDetails.getSurname();
    }
}
