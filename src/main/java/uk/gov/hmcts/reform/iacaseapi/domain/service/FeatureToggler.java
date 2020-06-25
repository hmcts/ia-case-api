package uk.gov.hmcts.reform.iacaseapi.domain.service;

public interface FeatureToggler {

    boolean getValue(String key, Boolean defaultValue);

}
