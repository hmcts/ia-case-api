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

    private static final String GRANTED = "granted";
    private static final String PARTIALLY_GRANTED = "partiallyGranted";
    private static final String REFUSED = "refused";
    private static final String NOT_ADMITTED = "notAdmitted";
    private static final String REHEARD_RULE_35 = "reheardRule35";
    private static final String REHEARD_RULE_32 = "reheardRule32";
    private static final String REMADE_RULE_32 = "remadeRule32";
    private static final String APPEAL_DECISION = "appealDecision";

    private final Map<Pair<String, String>, String> ftpaDisplayMap = new ImmutableMap.Builder<Pair<String, String>, String>()
        .put(new ImmutablePair<>(GRANTED, GRANTED), GRANTED)
        .put(new ImmutablePair<>(GRANTED, PARTIALLY_GRANTED), GRANTED)
        .put(new ImmutablePair<>(GRANTED, REFUSED), GRANTED)
        .put(new ImmutablePair<>(GRANTED, NOT_ADMITTED), GRANTED)
        .put(new ImmutablePair<>(GRANTED, REHEARD_RULE_35), GRANTED)
        .put(new ImmutablePair<>(GRANTED, REHEARD_RULE_32), GRANTED)
        .put(new ImmutablePair<>(GRANTED, REMADE_RULE_32), GRANTED)

        .put(new ImmutablePair<>(PARTIALLY_GRANTED, GRANTED), GRANTED)
        .put(new ImmutablePair<>(PARTIALLY_GRANTED, PARTIALLY_GRANTED), GRANTED)
        .put(new ImmutablePair<>(PARTIALLY_GRANTED, REFUSED), GRANTED)
        .put(new ImmutablePair<>(PARTIALLY_GRANTED, NOT_ADMITTED), GRANTED)
        .put(new ImmutablePair<>(PARTIALLY_GRANTED, REHEARD_RULE_35), GRANTED)
        .put(new ImmutablePair<>(PARTIALLY_GRANTED, REHEARD_RULE_32), GRANTED)
        .put(new ImmutablePair<>(PARTIALLY_GRANTED, REMADE_RULE_32), GRANTED)

        .put(new ImmutablePair<>(REFUSED, GRANTED), GRANTED)
        .put(new ImmutablePair<>(REFUSED, PARTIALLY_GRANTED), GRANTED)
        .put(new ImmutablePair<>(REFUSED, REFUSED), APPEAL_DECISION)
        .put(new ImmutablePair<>(REFUSED, NOT_ADMITTED), NOT_ADMITTED)
        .put(new ImmutablePair<>(REFUSED, REHEARD_RULE_35), REHEARD_RULE_35)
        .put(new ImmutablePair<>(REFUSED, REHEARD_RULE_32), REHEARD_RULE_32)
        .put(new ImmutablePair<>(REFUSED, REMADE_RULE_32), REMADE_RULE_32)

        .put(new ImmutablePair<>(NOT_ADMITTED, GRANTED), GRANTED)
        .put(new ImmutablePair<>(NOT_ADMITTED, PARTIALLY_GRANTED), GRANTED)
        .put(new ImmutablePair<>(NOT_ADMITTED, REFUSED), REFUSED)
        .put(new ImmutablePair<>(NOT_ADMITTED, NOT_ADMITTED), APPEAL_DECISION)
        .put(new ImmutablePair<>(NOT_ADMITTED, REHEARD_RULE_35), REHEARD_RULE_35)
        .put(new ImmutablePair<>(NOT_ADMITTED, REHEARD_RULE_32), REHEARD_RULE_32)
        .put(new ImmutablePair<>(NOT_ADMITTED, REMADE_RULE_32), REMADE_RULE_32)

        .put(new ImmutablePair<>(REHEARD_RULE_35, GRANTED), GRANTED)
        .put(new ImmutablePair<>(REHEARD_RULE_35, PARTIALLY_GRANTED), GRANTED)
        .put(new ImmutablePair<>(REHEARD_RULE_35, REFUSED), REHEARD_RULE_35)
        .put(new ImmutablePair<>(REHEARD_RULE_35, NOT_ADMITTED), REHEARD_RULE_35)
        .put(new ImmutablePair<>(REHEARD_RULE_35, REHEARD_RULE_35), REHEARD_RULE_35)
        .put(new ImmutablePair<>(REHEARD_RULE_35, REHEARD_RULE_32), REHEARD_RULE_32)
        .put(new ImmutablePair<>(REHEARD_RULE_35, REMADE_RULE_32), REMADE_RULE_32)

        .put(new ImmutablePair<>(REHEARD_RULE_32, GRANTED), GRANTED)
        .put(new ImmutablePair<>(REHEARD_RULE_32, PARTIALLY_GRANTED), GRANTED)
        .put(new ImmutablePair<>(REHEARD_RULE_32, REFUSED), REHEARD_RULE_32)
        .put(new ImmutablePair<>(REHEARD_RULE_32, NOT_ADMITTED), REHEARD_RULE_32)
        .put(new ImmutablePair<>(REHEARD_RULE_32, REHEARD_RULE_35), REHEARD_RULE_35)
        .put(new ImmutablePair<>(REHEARD_RULE_32, REHEARD_RULE_32), REHEARD_RULE_32)
        .put(new ImmutablePair<>(REHEARD_RULE_32, REMADE_RULE_32), REMADE_RULE_32)

        .put(new ImmutablePair<>(REMADE_RULE_32, GRANTED), GRANTED)
        .put(new ImmutablePair<>(REMADE_RULE_32, PARTIALLY_GRANTED), GRANTED)
        .put(new ImmutablePair<>(REMADE_RULE_32, REFUSED), REMADE_RULE_32)
        .put(new ImmutablePair<>(REMADE_RULE_32, NOT_ADMITTED), REMADE_RULE_32)
        .put(new ImmutablePair<>(REMADE_RULE_32, REHEARD_RULE_35), REHEARD_RULE_35)
        .put(new ImmutablePair<>(REMADE_RULE_32, REHEARD_RULE_32), REHEARD_RULE_32)
        .put(new ImmutablePair<>(REMADE_RULE_32, REMADE_RULE_32), REMADE_RULE_32)
        .build();

    public FtpaDisplayService(CaseFlagAppender caseFlagAppender) {
        this.caseFlagAppender = caseFlagAppender;
    }

    public String getFinalDisplayDecision(AsylumCase asylumCase, String firstDecision, String secondDecision) {

        if (ftpaDisplayMap.get(Pair.of(firstDecision, secondDecision)).equals(APPEAL_DECISION)) {
            return asylumCase.read(AsylumCaseFieldDefinition.APPEAL_DECISION)
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
