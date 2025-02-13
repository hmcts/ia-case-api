package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CURRENT_HEARING_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_ID_LIST;

@Slf4j
@Component
public class HearingIdListProcessor {
    public void processHearingIdList(AsylumCase asylumCase) {
        Optional<String> hearingIdOpt = asylumCase.read(CURRENT_HEARING_ID, String.class);
        if (hearingIdOpt.isPresent()) {
            String hearingId = hearingIdOpt.get();

            Optional<List<IdValue<String>>> hearingIdListOpt = asylumCase.read(HEARING_ID_LIST);
            final List<IdValue<String>> hearingIdList = hearingIdListOpt.orElse(emptyList());

            if (doesNotContainHearingId(hearingIdList, hearingId)) {
                List<IdValue<String>> newHearingIdList = appendToHearingIdList(hearingIdList, hearingId);
                asylumCase.write(HEARING_ID_LIST, newHearingIdList);
            }
        }
    }

    private boolean doesNotContainHearingId(
        List<IdValue<String>> existingHearingIdList,
        String newHearingId
    ) {
        for (IdValue<String> existingHearingId : existingHearingIdList) {
            if (newHearingId.equals(existingHearingId.getValue())) {
                return false;
            }
        }

        return true;
    }

    private List<IdValue<String>> appendToHearingIdList(
        List<IdValue<String>> existingHearingIdList,
        String newHearingId
    ) {
        final List<IdValue<String>> allHearingIds = new ArrayList<>();

        int index = 1;
        for (IdValue<String> existingHearingId : existingHearingIdList) {
            allHearingIds.add(new IdValue<>(String.valueOf(index++), existingHearingId.getValue()));
        }

        allHearingIds.add(new IdValue<>(String.valueOf(index), newHearingId));

        return allHearingIds;
    }
}
