package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRole;

public class IdamUserDetails implements UserDetails {

    private final String accessToken;
    private final String id;
    private final List<String> roles;
    private final String emailAddress;
    private final String forename;
    private final String surname;

    public IdamUserDetails(
        String accessToken,
        String id,
        List<String> roles,
        String emailAddress,
        String forename,
        String surname
    ) {
        requireNonNull(accessToken);
        requireNonNull(id);
        requireNonNull(roles);
        requireNonNull(emailAddress);
        requireNonNull(forename);
        requireNonNull(surname);

        this.accessToken = accessToken;
        this.id = id;
        this.roles = roles;
        this.emailAddress = emailAddress;
        this.forename = forename;
        this.surname = surname;
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

    @Override
    public boolean isAdmin() {
        return roles.stream().anyMatch(UserRole.getAdminRoles()::contains);
    }

    @Override
    public boolean isHomeOffice() {
        return roles.stream().anyMatch(UserRole.getHomeOfficeRoles()::contains);
    }

    @Override
    public String getEmailAddress() {
        return emailAddress;
    }

    @Override
    public String getForename() {
        return forename;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public String getForenameAndSurname() {
        return forename + " " + surname;
    }
}
