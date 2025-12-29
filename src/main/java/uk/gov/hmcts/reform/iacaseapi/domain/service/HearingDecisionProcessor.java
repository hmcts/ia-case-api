package uk.gov.hmcts.reform.iacaseapi.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_HEARING_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_DECISION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_DECISION_ALLOWED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.DECISION_GRANTED_OR_REFUSED;

@Component
@Slf4j
public class HearingDecisionProcessor {
    public void processHearingAppealDecision(AsylumCase asylumCase) {
        Optional<AppealDecision> appealDecisionOpt = asylumCase.read(IS_DECISION_ALLOWED, AppealDecision.class);
        String appealDecision;
        if (appealDecisionOpt.isPresent()) {
            appealDecision = appealDecisionOpt.get().getValue();
        } else {
            appealDecision = "decided";
        }
        processHearingDecision(asylumCase, appealDecision);
    }

    private void processHearingDecision(AsylumCase asylumCase, String decision) {
        Optional<String> currentHearingIdOpt = asylumCase.read(CURRENT_HEARING_ID, String.class);

        if (currentHearingIdOpt.isPresent()) {
            String currentHearingId = currentHearingIdOpt.get();

            Optional<List<IdValue<HearingDecision>>> hearingDecisionListOpt = asylumCase.read(HEARING_DECISION_LIST);
            final List<IdValue<HearingDecision>> hearingDecisionList = hearingDecisionListOpt.orElse(emptyList());

            Optional<IdValue<HearingDecision>> existingHearingDecisionIdValueOpt =
                    getHearingDecisionId(hearingDecisionList, currentHearingId);

            List<IdValue<HearingDecision>> newHearingDecisionList;
            if (existingHearingDecisionIdValueOpt.isPresent()) {
                IdValue<HearingDecision> existingHearingDecisionIdValue = existingHearingDecisionIdValueOpt.get();
                HearingDecision newHearingDecision = new HearingDecision(currentHearingId, decision);
                IdValue<HearingDecision> newHearingDecisionIdValue =
                        new IdValue<>(existingHearingDecisionIdValue.getId(), newHearingDecision);
                newHearingDecisionList =
                        updateHearingDecisionInHearingDecisionList(hearingDecisionList, newHearingDecisionIdValue);
            } else {
                HearingDecision newHearingDecision = new HearingDecision(currentHearingId, decision);
                newHearingDecisionList = appendToHearingDecisionList(hearingDecisionList, newHearingDecision);
            }
            asylumCase.write(HEARING_DECISION_LIST, newHearingDecisionList);
        }
    }

    public void processHearingDecision(BailCase bailCase) {
        Optional<String> decisionOpt = bailCase.read(DECISION_GRANTED_OR_REFUSED, String.class);
        String decision = decisionOpt.orElse("decided");
        processHearingDecision(bailCase, decision);
    }

    private void processHearingDecision(BailCase bailCase, String decision) {
        Optional<String> currentHearingIdOpt = bailCase.read(BailCaseFieldDefinition.CURRENT_HEARING_ID, String.class);

        if (currentHearingIdOpt.isPresent()) {
            String currentHearingId = currentHearingIdOpt.get();

            Optional<List<IdValue<HearingDecision>>> hearingDecisionListOpt = bailCase.read(BailCaseFieldDefinition.HEARING_DECISION_LIST);
            final List<IdValue<HearingDecision>> hearingDecisionList = hearingDecisionListOpt.orElse(emptyList());

            Optional<IdValue<HearingDecision>> existingHearingDecisionIdValueOpt =
                getHearingDecisionId(hearingDecisionList, currentHearingId);

            List<IdValue<HearingDecision>> newHearingDecisionList;
            if (existingHearingDecisionIdValueOpt.isPresent()) {
                IdValue<HearingDecision> existingHearingDecisionIdValue = existingHearingDecisionIdValueOpt.get();
                HearingDecision newHearingDecision = new HearingDecision(currentHearingId, decision);
                IdValue<HearingDecision> newHearingDecisionIdValue =
                    new IdValue<>(existingHearingDecisionIdValue.getId(), newHearingDecision);
                newHearingDecisionList =
                    updateHearingDecisionInHearingDecisionList(hearingDecisionList, newHearingDecisionIdValue);
            } else {
                HearingDecision newHearingDecision = new HearingDecision(currentHearingId, decision);
                newHearingDecisionList = appendToHearingDecisionList(hearingDecisionList, newHearingDecision);
            }
            bailCase.write(BailCaseFieldDefinition.HEARING_DECISION_LIST, newHearingDecisionList);
        }
    }

    private Optional<IdValue<HearingDecision>> getHearingDecisionId(
        List<IdValue<HearingDecision>> hearingDecisionIdList,
        String hearingDecisionId
    ) {
        for (IdValue<HearingDecision> existingHearingDecisionId : hearingDecisionIdList) {
            if (hearingDecisionId.equals(existingHearingDecisionId.getValue().getHearingId())) {
                return Optional.of(existingHearingDecisionId);
            }
        }

        return Optional.empty();
    }

    private List<IdValue<HearingDecision>> appendToHearingDecisionList(
        List<IdValue<HearingDecision>> existingHearingDecisionList,
        HearingDecision newHearingDecision
    ) {

        final List<IdValue<HearingDecision>> allHearingDecisions = new ArrayList<>();

        int index = 1;
        for (IdValue<HearingDecision> existingHearingDecision : existingHearingDecisionList) {
            allHearingDecisions.add(new IdValue<>(String.valueOf(index++), existingHearingDecision.getValue()));
        }

        allHearingDecisions.add(new IdValue<>(String.valueOf(index), newHearingDecision));

        return allHearingDecisions;
    }

    private List<IdValue<HearingDecision>> updateHearingDecisionInHearingDecisionList(
        List<IdValue<HearingDecision>> existingHearingDecisionList,
        IdValue<HearingDecision> updatedHearingDecisionIdValue
    ) {

        final List<IdValue<HearingDecision>> allHearingDecisions = new ArrayList<>();

        int index = 1;
        for (IdValue<HearingDecision> existingHearingDecision : existingHearingDecisionList) {
            if (String.valueOf(index).equals(updatedHearingDecisionIdValue.getId())) {
                allHearingDecisions.add(updatedHearingDecisionIdValue);
                index++;
            } else {
                allHearingDecisions.add(new IdValue<>(String.valueOf(index++), existingHearingDecision.getValue()));
            }
        }

        return allHearingDecisions;
    }
}
