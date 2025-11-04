package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.statutorytimeframe24weeks;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StatutoryTimeframe24Weeks;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STATUTORY_TIMEFRAME_24_WEEKS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STATUTORY_TIMEFRAME_24_WEEKS_REASON;

@Service
public class UpdateStatutoryTimeframe24WeeksService {

    private final Appender<StatutoryTimeframe24Weeks> statutoryTimeframe24WeeksAppender;
    private final DateProvider dateProvider;
    private final UserDetails userDetails;

    public UpdateStatutoryTimeframe24WeeksService(
        Appender<StatutoryTimeframe24Weeks> statutoryTimeframe24WeeksAppender,
        DateProvider dateProvider,
        UserDetails userDetails
    ) {
        this.statutoryTimeframe24WeeksAppender = statutoryTimeframe24WeeksAppender;
        this.dateProvider = dateProvider;
        this.userDetails = userDetails;
    }

    public AsylumCase updateAsylumCase(AsylumCase asylumCase, YesOrNo statutoryTimeframe24WeeksStatus) {
        String statutoryTimeframe24WeeksReason = asylumCase
            .read(STATUTORY_TIMEFRAME_24_WEEKS_REASON, String.class)
            .orElseThrow(() -> new IllegalStateException("statutoryTimeframe24WeeksReason is not present"));

        Optional<List<IdValue<StatutoryTimeframe24Weeks>>> maybeExistingStatutoryTimeframe24Weeks =
            asylumCase.read(STATUTORY_TIMEFRAME_24_WEEKS);

        final StatutoryTimeframe24Weeks newStatutoryTimeframe24Weeks = new StatutoryTimeframe24Weeks(
            statutoryTimeframe24WeeksStatus,
            statutoryTimeframe24WeeksReason,
            buildFullName(),
            dateProvider.nowWithTime().toString()
        );

        List<IdValue<StatutoryTimeframe24Weeks>> allStatutoryTimeframe24Weeks =
            statutoryTimeframe24WeeksAppender.append(newStatutoryTimeframe24Weeks, maybeExistingStatutoryTimeframe24Weeks.orElse(emptyList()));

        asylumCase.write(STATUTORY_TIMEFRAME_24_WEEKS, allStatutoryTimeframe24Weeks);
        asylumCase.clear(STATUTORY_TIMEFRAME_24_WEEKS_REASON);

        return asylumCase;
    }

    private String buildFullName() {
        return userDetails.getForename()
            + " "
            + userDetails.getSurname();
    }
}
