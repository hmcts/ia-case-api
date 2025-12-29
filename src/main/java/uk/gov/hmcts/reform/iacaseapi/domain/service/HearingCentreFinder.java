package uk.gov.hmcts.reform.iacaseapi.domain.service;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.util.Arrays;
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
    private final Map<HearingCentre, String> hearingCentreActivationDates;
    private final Map<HearingCentre, List<String>> hearingCentreMappings;

    public HearingCentreFinder(
        HearingCentre defaultHearingCentre,
        Map<HearingCentre, List<String>> hearingCentreCatchmentAreas,
        Map<HearingCentre, String> hearingCentreActivationDates,
        Map<HearingCentre, List<String>> hearingCentreMappings
    ) {
        this.defaultHearingCentre = defaultHearingCentre;
        this.hearingCentreCatchmentAreas = ImmutableMap.copyOf(hearingCentreCatchmentAreas);
        this.hearingCentreActivationDates = ImmutableMap.copyOf(hearingCentreActivationDates);
        this.hearingCentreMappings = ImmutableMap.copyOf(hearingCentreMappings);
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
                if (hearingCentreIsActive(hearingCentreActivationDates.get(hearingCentre.get()))) {
                    return hearingCentre.get();
                }
            }
        }

        return defaultHearingCentre;
    }

    public HearingCentre findByDetentionFacility(String detentionFacilityName) {

        Optional<HearingCentre> hearingCentre = hearingCentreMappings.entrySet().stream()
            .filter(mapping -> mapping.getValue().contains(
                detentionFacilityName)).map(Map.Entry::getKey).findFirst();

        if (hearingCentre.isPresent()) {

            return hearingCentre.get();

        }

        throw new RuntimeException("Prison or Immigration Removal Centre is not mapped to a Hearing Centre");

    }

    public boolean hearingCentreIsActive(String hearingCentreActivationDate) {
        if (LocalDate.parse(hearingCentreActivationDate).isAfter(LocalDate.now())) {
            return false;
        }
        return true;
    }

    public boolean hearingCentreIsActive(HearingCentre hearingCentre) {
        return hearingCentreIsActive(hearingCentreActivationDates.get(hearingCentre));
    }

    public boolean isListingOnlyHearingCentre(HearingCentre hearingCentre) {
        return Arrays.asList(
            HearingCentre.COVENTRY,
            HearingCentre.GLASGOW_TRIBUNALS_CENTRE,
            HearingCentre.NEWCASTLE,
            HearingCentre.BELFAST,
            HearingCentre.NOTTINGHAM,
            HearingCentre.HARMONDSWORTH,
            HearingCentre.HENDON,
            HearingCentre.YARLS_WOOD,
            HearingCentre.BRADFORD_KEIGHLEY,
            HearingCentre.MCC_MINSHULL,
            HearingCentre.MCC_CROWN_SQUARE,
            HearingCentre.MANCHESTER_MAGS,
            HearingCentre.NTH_TYNE_MAGS,
            HearingCentre.LEEDS_MAGS,
            HearingCentre.ALLOA_SHERRIF
        ).contains(hearingCentre);

    }
}
