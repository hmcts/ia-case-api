package uk.gov.hmcts.reform.iacaseapi.domain.service;

import com.launchdarkly.sdk.LDValue;

public interface FeatureToggler {

    boolean getValue(String key, Boolean defaultValue);

    LDValue getJsonValue(String key, LDValue defaultValue);

}
