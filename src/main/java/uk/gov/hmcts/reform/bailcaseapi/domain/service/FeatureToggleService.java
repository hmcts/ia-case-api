package uk.gov.hmcts.reform.bailcaseapi.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.LaunchDarklyFeatureToggler;

@Service
@RequiredArgsConstructor
public class FeatureToggleService {

    private final LaunchDarklyFeatureToggler launchDarklyFeatureToggler;

    public boolean imaEnabled() {

        return launchDarklyFeatureToggler.getValue("ima-feature-flag", false);
    }

    public boolean locationRefDataEnabled() {

        return launchDarklyFeatureToggler.getValue("bails-location-reference-data", false);
    }
}
