package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.PreviousHearing;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@Service
public class PreviousHearingAppender {

    public List<IdValue<PreviousHearing>> append(
        List<IdValue<PreviousHearing>> existingPreviousHearings,
        PreviousHearing newPreviousHearing
    ) {
        requireNonNull(existingPreviousHearings, "existingPreviousHearings must not be null");
        requireNonNull(newPreviousHearing, "newPreviousHearing must not be null");

        final List<IdValue<PreviousHearing>> allPreviousHearings = new ArrayList<>();

        int index = existingPreviousHearings.size() + 1;

        allPreviousHearings.add(new IdValue<>(String.valueOf(index--), newPreviousHearing));

        for (IdValue<PreviousHearing> existingPreviousHearing : existingPreviousHearings) {
            allPreviousHearings.add(new IdValue<>(String.valueOf(index--), existingPreviousHearing.getValue()));
        }

        return allPreviousHearings;
    }
}
