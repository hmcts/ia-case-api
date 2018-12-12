package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

public interface IdamUserConnectionConfig {
    String getAccessToken();

    String getId();
}
