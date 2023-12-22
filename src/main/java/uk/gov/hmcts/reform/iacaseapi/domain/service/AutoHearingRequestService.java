package uk.gov.hmcts.reform.iacaseapi.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutoHearingRequestService {

    private final FeatureToggler featureToggler;

    public boolean autoHearingRequestEnabled() {
        return featureToggler.getValue("auto-hearing-request-feature", false);
    }
}
