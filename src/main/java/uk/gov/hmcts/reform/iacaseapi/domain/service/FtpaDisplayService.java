package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;


@Service
public class FtpaDisplayService {

    private final CaseFlagAppender caseFlagAppender;

    private static final String granted = "granted";
    private static final String partiallyGranted = "partiallyGranted";
    private static final String refused = "refused";
    private static final String notAdmitted = "notAdmitted";
    private static final String reheardRule35 = "reheardRule35";
    private static final String reheardRule32 = "reheardRule32";
    private static final String remadeRule32 = "remadeRule32";
    private static final String appealDecision = "appealDecision";

    private final Map<Pair<String, String>, String> ftpaDisplayMap = new ImmutableMap.Builder<Pair<String, String>, String>()
        .put(new ImmutablePair<>(granted, granted), granted)
        .put(new ImmutablePair<>(granted, partiallyGranted), granted)
        .put(new ImmutablePair<>(granted, refused), granted)
        .put(new ImmutablePair<>(granted, notAdmitted), granted)
        .put(new ImmutablePair<>(granted, reheardRule35), granted)
        .put(new ImmutablePair<>(granted, reheardRule32), granted)
        .put(new ImmutablePair<>(granted, remadeRule32), granted)

        .put(new ImmutablePair<>(partiallyGranted, granted), granted)
        .put(new ImmutablePair<>(partiallyGranted, partiallyGranted), granted)
        .put(new ImmutablePair<>(partiallyGranted, refused), granted)
        .put(new ImmutablePair<>(partiallyGranted, notAdmitted), granted)
        .put(new ImmutablePair<>(partiallyGranted, reheardRule35), granted)
        .put(new ImmutablePair<>(partiallyGranted, reheardRule32), granted)
        .put(new ImmutablePair<>(partiallyGranted, remadeRule32), granted)

        .put(new ImmutablePair<>(refused, granted), granted)
        .put(new ImmutablePair<>(refused, partiallyGranted), granted)
        .put(new ImmutablePair<>(refused, refused), appealDecision)
        .put(new ImmutablePair<>(refused, notAdmitted), notAdmitted)
        .put(new ImmutablePair<>(refused, reheardRule35), reheardRule35)
        .put(new ImmutablePair<>(refused, reheardRule32), reheardRule32)
        .put(new ImmutablePair<>(refused, remadeRule32), remadeRule32)

        .put(new ImmutablePair<>(notAdmitted, granted), granted)
        .put(new ImmutablePair<>(notAdmitted, partiallyGranted), granted)
        .put(new ImmutablePair<>(notAdmitted, refused), refused)
        .put(new ImmutablePair<>(notAdmitted, notAdmitted), appealDecision)
        .put(new ImmutablePair<>(notAdmitted, reheardRule35), reheardRule35)
        .put(new ImmutablePair<>(notAdmitted, reheardRule32), reheardRule32)
        .put(new ImmutablePair<>(notAdmitted, remadeRule32), remadeRule32)

        .put(new ImmutablePair<>(reheardRule35, granted), granted)
        .put(new ImmutablePair<>(reheardRule35, partiallyGranted), granted)
        .put(new ImmutablePair<>(reheardRule35, refused), reheardRule35)
        .put(new ImmutablePair<>(reheardRule35, notAdmitted), reheardRule35)
        .put(new ImmutablePair<>(reheardRule35, reheardRule35), reheardRule35)
        .put(new ImmutablePair<>(reheardRule35, reheardRule32), reheardRule32)
        .put(new ImmutablePair<>(reheardRule35, remadeRule32), remadeRule32)

        .put(new ImmutablePair<>(reheardRule32, granted), granted)
        .put(new ImmutablePair<>(reheardRule32, partiallyGranted), granted)
        .put(new ImmutablePair<>(reheardRule32, refused), reheardRule32)
        .put(new ImmutablePair<>(reheardRule32, notAdmitted), reheardRule32)
        .put(new ImmutablePair<>(reheardRule32, reheardRule35), reheardRule35)
        .put(new ImmutablePair<>(reheardRule32, reheardRule32), reheardRule32)
        .put(new ImmutablePair<>(reheardRule32, remadeRule32), remadeRule32)

        .put(new ImmutablePair<>(remadeRule32, granted), granted)
        .put(new ImmutablePair<>(remadeRule32, partiallyGranted), granted)
        .put(new ImmutablePair<>(remadeRule32, refused), remadeRule32)
        .put(new ImmutablePair<>(remadeRule32, notAdmitted), remadeRule32)
        .put(new ImmutablePair<>(remadeRule32, reheardRule35), reheardRule35)
        .put(new ImmutablePair<>(remadeRule32, reheardRule32), reheardRule32)
        .put(new ImmutablePair<>(remadeRule32, remadeRule32), remadeRule32)
        .build();

    public FtpaDisplayService(CaseFlagAppender caseFlagAppender) {
        this.caseFlagAppender = caseFlagAppender;
    }

    public String getFinalDisplayDecision(AsylumCase asylumCase, String firstDecision, String secondDecision) {

        if (ftpaDisplayMap.get(Pair.of(firstDecision, secondDecision)).equals(appealDecision)) {
            return asylumCase.read(APPEAL_DECISION)
                .orElseThrow(() -> new IllegalStateException("appealDecision is mandatory"))
                .equals("Allowed") ? "allowed" : "dismissed";
        }

        return ftpaDisplayMap.get(Pair.of(firstDecision, secondDecision));
    }

    public void handleFtpaDecisions(AsylumCase asylumCase, String currentDecision, String ftpaFirstDecision) {

        if (!currentDecision.equals("")) {
            if (!ftpaFirstDecision.equals("")) {
                asylumCase.write(FTPA_SECOND_DECISION, currentDecision);
            } else {
                asylumCase.write(FTPA_FIRST_DECISION, currentDecision);
            }
        }

        setFinalDisplayDecision(
            asylumCase,
            asylumCase.read(FTPA_FIRST_DECISION, String.class).orElse(""),
            asylumCase.read(FTPA_SECOND_DECISION, String.class).orElse("")
        );
    }

    public void setFinalDisplayDecision(AsylumCase asylumCase, String firstDecision, String secondDecision) {

        if (!firstDecision.equals("") && !secondDecision.equals("")) {
            asylumCase.write(
                FTPA_FINAL_DECISION_FOR_DISPLAY, getFinalDisplayDecision(asylumCase, firstDecision, secondDecision));
        } else {
            asylumCase.write(
                FTPA_FINAL_DECISION_FOR_DISPLAY, "undecided");
        }
    }

    public void setFtpaCaseFlag(AsylumCase asylumCase, boolean isReheardAppealEnabled, String currentDecision) {

        asylumCase.write(AsylumCaseFieldDefinition.IS_REHEARD_APPEAL_ENABLED,
            isReheardAppealEnabled ? YesOrNo.YES : YesOrNo.NO);

        if (isReheardAppealEnabled && currentDecision.toLowerCase().contains("reheard")) {
            asylumCase.write(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YES);
            updateCaseFlags(asylumCase);
        }
    }

    protected void updateCaseFlags(AsylumCase asylumCase) {

        final CaseFlagType reheardSetAsideFlagType = CaseFlagType.SET_ASIDE_REHEARD;
        final Optional<List<IdValue<CaseFlag>>> maybeExistingCaseFlags = asylumCase.read(CASE_FLAGS);
        final List<IdValue<CaseFlag>> existingCaseFlags = maybeExistingCaseFlags.orElse(Collections.emptyList());

        final List<IdValue<CaseFlag>> allCaseFlags = caseFlagAppender.append(
            existingCaseFlags,
            reheardSetAsideFlagType,
            ""
        );

        asylumCase.write(CASE_FLAGS, allCaseFlags);
    }
}
