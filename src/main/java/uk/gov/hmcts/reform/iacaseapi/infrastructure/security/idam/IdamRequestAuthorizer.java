package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static java.util.regex.Pattern.compile;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.BearerTokenInvalidException;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.UnauthorisedRoleException;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;

@Slf4j
public class IdamRequestAuthorizer implements RequestAuthorizer<User> {

    public static final String AUTHORISATION = "Authorization";
    private static final Pattern TOKEN_PATTERN = compile("^Bearer [^\\s]+$");

    private final Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor;
    private final UserDetailsProvider userDetailsProvider;
    private final IdamTokenVerification idamTokenVerification;

    public IdamRequestAuthorizer(Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor, UserDetailsProvider userDetailsProvider, IdamTokenVerification idamTokenVerification) {
        this.authorizedRolesExtractor = authorizedRolesExtractor;
        this.userDetailsProvider = userDetailsProvider;
        this.idamTokenVerification = idamTokenVerification;
    }

    @Override
    public User authorise(HttpServletRequest request) {

        User user = Optional.ofNullable(request.getHeader(AUTHORISATION))
            .filter(token -> TOKEN_PATTERN.matcher(token).matches())
            .filter(idamTokenVerification::verifyTokenSignature)
            .map(this::fetchUser)
            .orElseThrow(() -> new BearerTokenInvalidException(new RuntimeException()));

        Collection<String> authorizedRoles = authorizedRolesExtractor.apply(request);

        if (!authorizedRoles.isEmpty() && Collections.disjoint(authorizedRoles, user.getRoles())) {
            throw new UnauthorisedRoleException();
        }

        return user;
    }

    private User fetchUser(String bearerToken) {

        try {

            UserDetails userDetails = userDetailsProvider.getUserDetails(bearerToken);
            return new User(userDetails.getId(), new HashSet<>(userDetails.getRoles()));

        } catch (IdentityManagerResponseException e) {

            log.error("Cannot fetch user from auth provider", e);
            return new User("", Collections.emptySet());
        }

    }

}
