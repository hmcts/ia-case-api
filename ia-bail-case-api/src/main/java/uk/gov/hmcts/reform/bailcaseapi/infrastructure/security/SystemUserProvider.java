package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security;

public interface SystemUserProvider {

    String getSystemUserId(String userToken);
}
