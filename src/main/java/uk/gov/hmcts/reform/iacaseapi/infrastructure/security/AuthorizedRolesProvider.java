package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import java.util.Set;

public interface AuthorizedRolesProvider {

    Set<String> getRoles();

}
