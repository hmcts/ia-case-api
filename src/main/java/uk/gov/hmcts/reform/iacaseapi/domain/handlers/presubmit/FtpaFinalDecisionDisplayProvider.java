package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;


@Component
public class FtpaFinalDecisionDisplayProvider {

    private final Map<Pair, String> ftpaDisplayMap = new ImmutableMap.Builder<Pair, String>()
        .put(new ImmutablePair<>("granted", "granted"), "granted")
        .put(new ImmutablePair<>("granted", "partiallyGranted"), "granted")
        .put(new ImmutablePair<>("granted", "refused"), "granted")
        .put(new ImmutablePair<>("granted", "notAdmitted"), "granted")
        .put(new ImmutablePair<>("granted", "reheardRule35"), "granted")
        .put(new ImmutablePair<>("granted", "reheardRule32"), "granted")
        .put(new ImmutablePair<>("granted", "remadeRule32"), "granted")

        .put(new ImmutablePair<>("partiallyGranted", "granted"), "granted")
        .put(new ImmutablePair<>("partiallyGranted", "partiallyGranted"), "granted")
        .put(new ImmutablePair<>("partiallyGranted", "refused"), "granted")
        .put(new ImmutablePair<>("partiallyGranted", "notAdmitted"), "granted")
        .put(new ImmutablePair<>("partiallyGranted", "reheardRule35"), "granted")
        .put(new ImmutablePair<>("partiallyGranted", "reheardRule32"), "granted")
        .put(new ImmutablePair<>("partiallyGranted", "remadeRule32"), "granted")

        .put(new ImmutablePair<>("refused", "granted"), "granted")
        .put(new ImmutablePair<>("refused", "partiallyGranted"), "granted")
        .put(new ImmutablePair<>("refused", "refused"), "appealDecision")
        .put(new ImmutablePair<>("refused", "notAdmitted"), "notAdmitted")
        .put(new ImmutablePair<>("refused", "reheardRule35"), "reheardRule35")
        .put(new ImmutablePair<>("refused", "reheardRule32"), "reheardRule32")
        .put(new ImmutablePair<>("refused", "remadeRule32"), "remadeRule32")

        .put(new ImmutablePair<>("notAdmitted", "granted"), "granted")
        .put(new ImmutablePair<>("notAdmitted", "partiallyGranted"), "granted")
        .put(new ImmutablePair<>("notAdmitted", "refused"), "refused")
        .put(new ImmutablePair<>("notAdmitted", "notAdmitted"), "appealDecision")
        .put(new ImmutablePair<>("notAdmitted", "reheardRule35"), "reheardRule35")
        .put(new ImmutablePair<>("notAdmitted", "reheardRule32"), "reheardRule32")
        .put(new ImmutablePair<>("notAdmitted", "remadeRule32"), "remadeRule32")

        .put(new ImmutablePair<>("reheardRule35", "granted"), "granted")
        .put(new ImmutablePair<>("reheardRule35", "partiallyGranted"), "granted")
        .put(new ImmutablePair<>("reheardRule35", "refused"), "reheardRule35")
        .put(new ImmutablePair<>("reheardRule35", "notAdmitted"), "reheardRule35")
        .put(new ImmutablePair<>("reheardRule35", "reheardRule35"), "reheardRule35")
        .put(new ImmutablePair<>("reheardRule35", "reheardRule32"), "reheardRule32")
        .put(new ImmutablePair<>("reheardRule35", "remadeRule32"), "remadeRule32")

        .put(new ImmutablePair<>("reheardRule32", "granted"), "granted")
        .put(new ImmutablePair<>("reheardRule32", "partiallyGranted"), "granted")
        .put(new ImmutablePair<>("reheardRule32", "refused"), "reheardRule32")
        .put(new ImmutablePair<>("reheardRule32", "notAdmitted"), "reheardRule32")
        .put(new ImmutablePair<>("reheardRule32", "reheardRule35"), "reheardRule35")
        .put(new ImmutablePair<>("reheardRule32", "reheardRule32"), "reheardRule32")
        .put(new ImmutablePair<>("reheardRule32", "remadeRule32"), "remadeRule32")

        .put(new ImmutablePair<>("remadeRule32", "granted"), "granted")
        .put(new ImmutablePair<>("remadeRule32", "partiallyGranted"), "granted")
        .put(new ImmutablePair<>("remadeRule32", "refused"), "remadeRule32")
        .put(new ImmutablePair<>("remadeRule32", "notAdmitted"), "remadeRule32")
        .put(new ImmutablePair<>("remadeRule32", "reheardRule35"), "reheardRule35")
        .put(new ImmutablePair<>("remadeRule32", "reheardRule32"), "reheardRule32")
        .put(new ImmutablePair<>("remadeRule32", "remadeRule32"), "remadeRule32")
        .build();

    public String getFinalDisplayDecision(AsylumCase asylumCase, String firstDecision, String secondDecision) {

        if (ftpaDisplayMap.get(Pair.of(firstDecision, secondDecision)).equals("appealDecision")) {
            return asylumCase.read(APPEAL_DECISION).get().equals("Allowed") ? "allowed" : "dismissed";
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
}
