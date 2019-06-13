package uk.gov.hmcts.reform.iacaseapi.domain.service;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;

public class HearingCentreFinder {

    private static final Pattern POSTCODE_AREA_PATTERN = Pattern.compile("([A-Za-z]+).*");

    private final HearingCentre defaultHearingCentre;
    private final Map<HearingCentre, List<String>> hearingCentreCatchmentAreas;

    public HearingCentreFinder(
        HearingCentre defaultHearingCentre,
        Map<HearingCentre, List<String>> hearingCentreCatchmentAreas
    ) {
        this.defaultHearingCentre = defaultHearingCentre;
        this.hearingCentreCatchmentAreas = ImmutableMap.copyOf(hearingCentreCatchmentAreas);
    }

    public HearingCentre getDefaultHearingCentre() {
        return defaultHearingCentre;
    }

    public HearingCentre find(
        String postcode
    ) {
        Matcher postcodeAreaMatcher = POSTCODE_AREA_PATTERN.matcher(postcode);

        if (postcodeAreaMatcher.find()
            && postcodeAreaMatcher.groupCount() == 1) {

            String postcodeArea = postcodeAreaMatcher.group(1).toUpperCase();

            Optional<HearingCentre> hearingCentre =
                hearingCentreCatchmentAreas
                    .entrySet()
                    .stream()
                    .filter(catchmentArea -> catchmentArea.getValue().contains(postcodeArea))
                    .map(Map.Entry::getKey)
                    .findFirst();

            if (hearingCentre.isPresent()) {
                return hearingCentre.get();
            }
        }

        return defaultHearingCentre;
    }
}
