package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security;

import java.util.Set;

public interface AuthorizedRolesProvider {

    Set<String> getRoles();
}
