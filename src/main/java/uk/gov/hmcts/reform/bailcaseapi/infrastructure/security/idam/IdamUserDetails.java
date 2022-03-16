package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security.idam;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.UserDetails;

public class IdamUserDetails implements UserDetails {

    private final String accessToken;
    private final String id;
    private final List<String> roles;


    public IdamUserDetails(
        String accessToken, String id,
        List<String> roles

    ) {
        this.accessToken = accessToken;
        requireNonNull(id);
        requireNonNull(roles);

        this.id = id;
        this.roles = roles;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<String> getRoles() {
        return Collections.unmodifiableList(roles);
    }

}
