package uk.gov.hmcts.reform.iacaseapi.infrastructure.config;

import java.util.HashMap;
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

    private Map<HearingCentre, List<String>> hearingCentreCatchmentAreas = new HashMap<>();

    public Map<HearingCentre, List<String>> getHearingCentreCatchmentAreas() {
        return hearingCentreCatchmentAreas;
    }

    @Bean
    public HearingCentreFinder hearingCentreFinder(
        @Value("${defaultHearingCentre}") String defaultHearingCentre
    ) {
        return new HearingCentreFinder(
            HearingCentre
                .from(defaultHearingCentre)
                .orElseThrow(() -> new IllegalArgumentException("Hearing centre not found")),
            hearingCentreCatchmentAreas
        );
    }
}
