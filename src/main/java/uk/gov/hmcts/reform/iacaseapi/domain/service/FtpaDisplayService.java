package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_SET_ASIDE_REHEARD_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_FINAL_DECISION_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_FIRST_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.FTPA_SECOND_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGACY_CASE_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SECOND_FTPA_DECISION_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STITCHING_STATUS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.valueOf;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.FtpaApplications;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.LegacyCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.FtpaDecisionCheckValues;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
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
    private static final String REMADE_RULE_31 = "remadeRule31";
    private static final String REMADE_RULE_32 = "remadeRule32";
    private static final String APPEAL_DECISION = "appealDecision";

    private final Map<Pair<String, String>, String> ftpaDisplayMap = new ImmutableMap.Builder<Pair<String, String>, String>()
        .put(new ImmutablePair<>(GRANTED, GRANTED), GRANTED)
        .put(new ImmutablePair<>(GRANTED, PARTIALLY_GRANTED), GRANTED)
        .put(new ImmutablePair<>(GRANTED, REFUSED), GRANTED)
        .put(new ImmutablePair<>(GRANTED, NOT_ADMITTED), GRANTED)
        .put(new ImmutablePair<>(GRANTED, REHEARD_RULE_35), GRANTED)
        .put(new ImmutablePair<>(GRANTED, REHEARD_RULE_32), GRANTED)
        .put(new ImmutablePair<>(GRANTED, REMADE_RULE_31), GRANTED)
        .put(new ImmutablePair<>(GRANTED, REMADE_RULE_32), GRANTED)

        .put(new ImmutablePair<>(PARTIALLY_GRANTED, GRANTED), GRANTED)
        .put(new ImmutablePair<>(PARTIALLY_GRANTED, PARTIALLY_GRANTED), GRANTED)
        .put(new ImmutablePair<>(PARTIALLY_GRANTED, REFUSED), GRANTED)
        .put(new ImmutablePair<>(PARTIALLY_GRANTED, NOT_ADMITTED), GRANTED)
        .put(new ImmutablePair<>(PARTIALLY_GRANTED, REHEARD_RULE_35), GRANTED)
        .put(new ImmutablePair<>(PARTIALLY_GRANTED, REHEARD_RULE_32), GRANTED)
        .put(new ImmutablePair<>(PARTIALLY_GRANTED, REMADE_RULE_31), GRANTED)
        .put(new ImmutablePair<>(PARTIALLY_GRANTED, REMADE_RULE_32), GRANTED)

        .put(new ImmutablePair<>(REFUSED, GRANTED), GRANTED)
        .put(new ImmutablePair<>(REFUSED, PARTIALLY_GRANTED), GRANTED)
        .put(new ImmutablePair<>(REFUSED, REFUSED), APPEAL_DECISION)
        .put(new ImmutablePair<>(REFUSED, NOT_ADMITTED), NOT_ADMITTED)
        .put(new ImmutablePair<>(REFUSED, REHEARD_RULE_35), REHEARD_RULE_35)
        .put(new ImmutablePair<>(REFUSED, REHEARD_RULE_32), REHEARD_RULE_32)
        .put(new ImmutablePair<>(REFUSED, REMADE_RULE_31), REMADE_RULE_31)
        .put(new ImmutablePair<>(REFUSED, REMADE_RULE_32), REMADE_RULE_32)

        .put(new ImmutablePair<>(NOT_ADMITTED, GRANTED), GRANTED)
        .put(new ImmutablePair<>(NOT_ADMITTED, PARTIALLY_GRANTED), GRANTED)
        .put(new ImmutablePair<>(NOT_ADMITTED, REFUSED), REFUSED)
        .put(new ImmutablePair<>(NOT_ADMITTED, NOT_ADMITTED), APPEAL_DECISION)
        .put(new ImmutablePair<>(NOT_ADMITTED, REHEARD_RULE_35), REHEARD_RULE_35)
        .put(new ImmutablePair<>(NOT_ADMITTED, REHEARD_RULE_32), REHEARD_RULE_32)
        .put(new ImmutablePair<>(NOT_ADMITTED, REMADE_RULE_31), REMADE_RULE_31)
        .put(new ImmutablePair<>(NOT_ADMITTED, REMADE_RULE_32), REMADE_RULE_32)

        .put(new ImmutablePair<>(REHEARD_RULE_35, GRANTED), GRANTED)
        .put(new ImmutablePair<>(REHEARD_RULE_35, PARTIALLY_GRANTED), GRANTED)
        .put(new ImmutablePair<>(REHEARD_RULE_35, REFUSED), REHEARD_RULE_35)
        .put(new ImmutablePair<>(REHEARD_RULE_35, NOT_ADMITTED), REHEARD_RULE_35)
        .put(new ImmutablePair<>(REHEARD_RULE_35, REHEARD_RULE_35), REHEARD_RULE_35)
        .put(new ImmutablePair<>(REHEARD_RULE_35, REHEARD_RULE_32), REHEARD_RULE_32)
        .put(new ImmutablePair<>(REHEARD_RULE_35, REMADE_RULE_31), REMADE_RULE_31)
        .put(new ImmutablePair<>(REHEARD_RULE_35, REMADE_RULE_32), REMADE_RULE_32)

        .put(new ImmutablePair<>(REHEARD_RULE_32, GRANTED), GRANTED)
        .put(new ImmutablePair<>(REHEARD_RULE_32, PARTIALLY_GRANTED), GRANTED)
        .put(new ImmutablePair<>(REHEARD_RULE_32, REFUSED), REHEARD_RULE_32)
        .put(new ImmutablePair<>(REHEARD_RULE_32, NOT_ADMITTED), REHEARD_RULE_32)
        .put(new ImmutablePair<>(REHEARD_RULE_32, REHEARD_RULE_35), REHEARD_RULE_35)
        .put(new ImmutablePair<>(REHEARD_RULE_32, REHEARD_RULE_32), REHEARD_RULE_32)
        .put(new ImmutablePair<>(REHEARD_RULE_32, REMADE_RULE_31), REMADE_RULE_31)
        .put(new ImmutablePair<>(REHEARD_RULE_32, REMADE_RULE_32), REMADE_RULE_32)

        .put(new ImmutablePair<>(REMADE_RULE_31, GRANTED), GRANTED)
        .put(new ImmutablePair<>(REMADE_RULE_31, PARTIALLY_GRANTED), GRANTED)
        .put(new ImmutablePair<>(REMADE_RULE_31, REFUSED), REMADE_RULE_31)
        .put(new ImmutablePair<>(REMADE_RULE_31, NOT_ADMITTED), REMADE_RULE_31)
        .put(new ImmutablePair<>(REMADE_RULE_31, REHEARD_RULE_35), REHEARD_RULE_35)
        .put(new ImmutablePair<>(REMADE_RULE_31, REHEARD_RULE_32), REHEARD_RULE_32)
        .put(new ImmutablePair<>(REMADE_RULE_31, REMADE_RULE_31), REMADE_RULE_31)
        .put(new ImmutablePair<>(REMADE_RULE_31, REMADE_RULE_32), REMADE_RULE_32)

        .put(new ImmutablePair<>(REMADE_RULE_32, GRANTED), GRANTED)
        .put(new ImmutablePair<>(REMADE_RULE_32, PARTIALLY_GRANTED), GRANTED)
        .put(new ImmutablePair<>(REMADE_RULE_32, REFUSED), REMADE_RULE_32)
        .put(new ImmutablePair<>(REMADE_RULE_32, NOT_ADMITTED), REMADE_RULE_32)
        .put(new ImmutablePair<>(REMADE_RULE_32, REHEARD_RULE_35), REHEARD_RULE_35)
        .put(new ImmutablePair<>(REMADE_RULE_32, REHEARD_RULE_32), REHEARD_RULE_32)
        .put(new ImmutablePair<>(REMADE_RULE_32, REMADE_RULE_31), REMADE_RULE_31)
        .put(new ImmutablePair<>(REMADE_RULE_32, REMADE_RULE_32), REMADE_RULE_32)
        .build();

    public FtpaDisplayService(CaseFlagAppender caseFlagAppender
    ) {
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
                asylumCase.write(SECOND_FTPA_DECISION_EXISTS, YesOrNo.YES);
            } else {
                asylumCase.write(FTPA_FIRST_DECISION, currentDecision);
                asylumCase.write(SECOND_FTPA_DECISION_EXISTS, YesOrNo.NO);
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
            asylumCase.write(STITCHING_STATUS,"");
            updateCaseFlags(asylumCase);
        }
    }

    public void setFtpaCaseDlrmFlag(AsylumCase asylumCase, boolean isDlrmFeatureEnabled) {

        asylumCase.write(AsylumCaseFieldDefinition.IS_DLRM_SET_ASIDE_ENABLED,
            isDlrmFeatureEnabled ? YesOrNo.YES : YesOrNo.NO);

    }

    public void mapFtpaDecision(boolean isMigration, AsylumCase asylumCase, String ftpaApplicantType, FtpaApplications ftpaApplication) {

        ftpaApplication.setIsFtpaNoticeOfDecisionSetAside(asylumCase
                .read(valueOf(String.format("IS_FTPA_%s_NOTICE_OF_DECISION_SET_ASIDE", ftpaApplicantType)), YesOrNo.class)
                .orElse(YesOrNo.NO));

        String ftpaDecisionOutcomeType = asylumCase.read(
                        valueOf(String.format("FTPA_%s_RJ_DECISION_OUTCOME_TYPE", ftpaApplicantType)), String.class)
                .orElseThrow(() -> new IllegalStateException("ftpaDecisionOutcomeType is not present"));
        ftpaApplication.setFtpaDecisionOutcomeType(ftpaDecisionOutcomeType);
        addFtpaDecisionAndReasons(isMigration, asylumCase, ftpaApplicantType, ftpaApplication);

        final Optional<List<IdValue<DocumentWithDescription>>> maybeFtpaDecisionNoticeDocument = asylumCase.read(
                valueOf(String.format("FTPA_%s_NOTICE_DOCUMENT", ftpaApplicantType)));
        final Optional<FtpaDecisionCheckValues<String>> maybeDecisionNotesPoints =
                asylumCase.read(valueOf(String.format("FTPA_%s_RJ_DECISION_NOTES_POINTS", ftpaApplicantType)));

        maybeFtpaDecisionNoticeDocument.ifPresent(ftpaApplication::setFtpaNoticeDocument);
        maybeDecisionNotesPoints.ifPresent(ftpaApplication::setFtpaDecisionNotesPoints);

        asylumCase.read(valueOf(String.format("FTPA_%s_DECISION_OBJECTIONS", ftpaApplicantType)), String.class)
                .ifPresent(ftpaApplication::setFtpaDecisionObjections);
        asylumCase.read(valueOf(String.format("FTPA_%s_RJ_DECISION_NOTES_DESCRIPTION", ftpaApplicantType)), String.class)
                .ifPresent(ftpaApplication::setFtpaDecisionNotesDescription);
        asylumCase.read(valueOf(String.format("FTPA_%s_DECISION_DATE", ftpaApplicantType)), String.class)
                .ifPresent(ftpaApplication::setFtpaDecisionDate);

        if (!isMigration) {
            if (ftpaDecisionOutcomeType.equals("remadeRule31") || ftpaDecisionOutcomeType.equals("remadeRule32")) {
                asylumCase.read(valueOf(String.format("FTPA_%s_DECISION_REMADE_RULE_32_TEXT", ftpaApplicantType)),
                                String.class)
                        .ifPresent(ftpaApplication::setFtpaDecisionRemadeRule32Text);
            }
            if (ftpaDecisionOutcomeType.equals("reheardRule35")) {
                addRule35Data(asylumCase, ftpaApplicantType, ftpaApplication);
            }
        } else {
            if (ftpaDecisionOutcomeType.equals("remadeRule32")) {
                asylumCase.read(valueOf(String.format("FTPA_%s_DECISION_REMADE_RULE_32", ftpaApplicantType)), String.class)
                        .ifPresent(ftpaApplication::setFtpaDecisionRemadeRule32);
            }
            asylumCase.read(valueOf(String.format("FTPA_%s_DECISION_LST_INS", ftpaApplicantType)), String.class)
                    .ifPresent(ftpaApplication::setFtpaDecisionLstIns);
        }

    }

    private void addRule35Data(AsylumCase asylumCase, String ftpaApplicantType, FtpaApplications ftpaApplication) {
        final Document rule35NoticeDocument =
                asylumCase.read(
                        valueOf(String.format("FTPA_R35_%s_DOCUMENT", ftpaApplicantType)), Document.class)
                        .orElse(null);

        final Optional<List<IdValue<DocumentWithDescription>>> maybeFtpaR35DecisionNoticeDocument =
                asylumCase.read(valueOf(String.format("FTPA_%s_R35_NOTICE_DOCUMENT", ftpaApplicantType)));

        maybeFtpaR35DecisionNoticeDocument.ifPresent(ftpaApplication::setFtpaNoticeDocument);
        ftpaApplication.setFtpaR35Document(rule35NoticeDocument);
        ftpaApplication.setFtpaDecisionOutcomeTypeR35("Review decision under rule 35");
        ftpaApplication.setFtpaDecisionOutcomeType(null);
        ftpaApplication.setIsFtpaNoticeOfDecisionSetAside(null);
        asylumCase.read(valueOf(String.format("FTPA_%s_R35_LISTING_ADDITIONAL_INS", ftpaApplicantType)), String.class)
                .ifPresent(ftpaApplication::setFtpaDecisionLstIns);
        asylumCase.read(valueOf(String.format("FTPA_%s_R35_DECISION_OBJECTIONS", ftpaApplicantType)), String.class)
                .ifPresent(ftpaApplication::setFtpaDecisionObjections);
    }

    private void addFtpaDecisionAndReasons(boolean isMigration, AsylumCase asylumCase,
                                           String ftpaApplicantType, FtpaApplications ftpaApplication) {

        if (isMigration) {
            final Optional<List<IdValue<DocumentWithDescription>>> maybeFtpaDecisionAndReasonsDocument = asylumCase.read(
                    valueOf(String.format("FTPA_%s_DECISION_DOCUMENT", ftpaApplicantType)));
            maybeFtpaDecisionAndReasonsDocument.ifPresent(ftpaApplication::setFtpaLegacyDecisionDocument);
        } else {
            final Document ftpaDecisionDocument =
                    asylumCase.read(
                            valueOf(String.format("FTPA_APPLICATION_%s_DOCUMENT", ftpaApplicantType)), Document.class)
                            .orElse(null);

            ftpaApplication.setFtpaNewDecisionDocument(ftpaDecisionDocument);
        }
    }

    protected void updateCaseFlags(AsylumCase asylumCase) {

        final CaseFlagType reheardSetAsideFlagType = CaseFlagType.SET_ASIDE_REHEARD;
        final Optional<List<IdValue<LegacyCaseFlag>>> maybeExistingCaseFlags = asylumCase.read(LEGACY_CASE_FLAGS);
        final List<IdValue<LegacyCaseFlag>> existingCaseFlags = maybeExistingCaseFlags.orElse(Collections.emptyList());

        final List<IdValue<LegacyCaseFlag>> allCaseFlags = caseFlagAppender.append(
            existingCaseFlags,
            reheardSetAsideFlagType,
            ""
        );

        asylumCase.write(LEGACY_CASE_FLAGS, allCaseFlags);
    }
}
