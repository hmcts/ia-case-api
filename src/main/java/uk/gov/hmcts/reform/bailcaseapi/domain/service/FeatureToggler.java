package uk.gov.hmcts.reform.bailcaseapi.domain.service;

public interface FeatureToggler {

    boolean getValue(String key, Boolean defaultValue);

}
