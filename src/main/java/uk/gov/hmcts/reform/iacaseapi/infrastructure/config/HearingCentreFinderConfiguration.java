package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;

@Configuration
@ConfigurationProperties
public class HearingCentreFinderConfiguration {

    private Map<HearingCentre, List<String>> hearingCentreCatchmentAreas = new EnumMap<>(HearingCentre.class);
    private Map<HearingCentre, String> hearingCentreActivationDates = new EnumMap<>(HearingCentre.class);
    private Map<HearingCentre, List<String>> hearingCentreMappings = new EnumMap<>(HearingCentre.class);

    public Map<HearingCentre, List<String>> getHearingCentreCatchmentAreas() {
        return Collections.unmodifiableMap(hearingCentreCatchmentAreas);
    }

    public void setHearingCentreCatchmentAreas(Map<HearingCentre, List<String>> hearingCentreCatchmentAreas) {
        this.hearingCentreCatchmentAreas = hearingCentreCatchmentAreas;
    }

    public Map<HearingCentre, String> getHearingCentreActivationDates() {
        return Collections.unmodifiableMap(hearingCentreActivationDates);
    }

    public void setHearingCentreActivationDates(Map<HearingCentre, String> hearingCentreActivationDates) {
        this.hearingCentreActivationDates = hearingCentreActivationDates;
    }

    public Map<HearingCentre, List<String>> getHearingCentreMappings() {
        return Collections.unmodifiableMap(hearingCentreMappings);
    }

    public void setHearingCentreMappings(Map<HearingCentre, List<String>> hearingCentreMappings) {
        this.hearingCentreMappings = hearingCentreMappings;
    }


    @Bean
    public HearingCentreFinder hearingCentreFinder(
        @Value("${defaultHearingCentre}") String defaultHearingCentre
    ) {
        return new HearingCentreFinder(
            HearingCentre
                .from(defaultHearingCentre)
                .orElseThrow(() -> new IllegalArgumentException("Hearing centre not found")),
            hearingCentreCatchmentAreas,
            hearingCentreActivationDates,
            hearingCentreMappings
        );
    }
}
