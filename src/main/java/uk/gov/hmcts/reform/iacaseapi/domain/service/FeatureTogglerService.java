package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.springframework.stereotype.Component;

@Component
public class FeatureTogglerService {

    private FeatureToggler featureToggler;

    public FeatureTogglerService(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    public boolean getValueForMakeAnApplicationFeature() {
        return featureToggler.getValue("make-an-application-feature", false);
    }
}
