package uk.gov.hmcts.reform.iacaseapi.domain.service;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;

public class HearingCentreFinder {

    private final Map<HearingCentre, List<String>> hearingCentreMappings;

    public HearingCentreFinder(

        Map<HearingCentre, List<String>> hearingCentreMappings) {
        this.hearingCentreMappings = ImmutableMap.copyOf(hearingCentreMappings);
    }

    public HearingCentre find(String detentionFacilityName) {

        Optional<HearingCentre> hearingCentre = hearingCentreMappings.entrySet().stream()
            .filter(mapping -> mapping.getValue().contains(
            detentionFacilityName)).map(Map.Entry::getKey).findFirst();

        if (hearingCentre.isPresent()) {

            return hearingCentre.get();

        }

        throw new RuntimeException("Prison or Immigration Removal Centre is not mapped to a Hearing Centre");

    }
}
