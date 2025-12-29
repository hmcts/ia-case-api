package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

public interface SystemUserProvider {

    String getSystemUserId(String userToken);
}
