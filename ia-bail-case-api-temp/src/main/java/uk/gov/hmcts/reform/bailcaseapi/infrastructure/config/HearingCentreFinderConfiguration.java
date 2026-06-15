package uk.gov.hmcts.reform.bailcaseapi.infrastructure.config;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.HearingCentreFinder;

@Configuration
@ConfigurationProperties
public class HearingCentreFinderConfiguration {

    private Map<HearingCentre, List<String>> hearingCentreMappings = new EnumMap<>(HearingCentre.class);

    public Map<HearingCentre, List<String>> getHearingCentreMappings() {
        return hearingCentreMappings;
    }

    @Bean
    public HearingCentreFinder hearingCentreFinder() {
        return new HearingCentreFinder(hearingCentreMappings);
    }
}
