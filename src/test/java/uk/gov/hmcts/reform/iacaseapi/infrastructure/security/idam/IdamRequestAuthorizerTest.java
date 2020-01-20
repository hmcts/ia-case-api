package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.BearerTokenInvalidException;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.UnauthorisedRoleException;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;

@RunWith(MockitoJUnitRunner.class)
public class IdamRequestAuthorizerTest {


    @Mock Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor;
    @Mock UserDetailsProvider userDetailsProvider;
    @Mock IdamTokenVerification idamTokenVerification;
    @Mock HttpServletRequest request;

    private String validToken = "Bearer validToken";
    private String role1 = "role1";
    private String role2 = "role2";
    private UserDetails userDetails = new IdamUserDetails(
        validToken,
        "someId",
        Lists.newArrayList(role1, role2),
        "someEmail",
        "someGivenName",
        "someFamilyName"
    );

    private IdamRequestAuthorizer idamRequestAuthorizer;

    @Before
    public void setup() {

        when(request.getHeader(IdamRequestAuthorizer.AUTHORISATION)).thenReturn(validToken);
        when(idamTokenVerification.verifyTokenSignature(validToken)).thenReturn(true);
        when(authorizedRolesExtractor.apply(request)).thenReturn(new HashSet<>(Arrays.asList(role1, role2)));
        when(userDetailsProvider.getUserDetails(validToken)).thenReturn(userDetails);

        idamRequestAuthorizer = new IdamRequestAuthorizer(authorizedRolesExtractor, userDetailsProvider, idamTokenVerification);
    }

    @Test
    public void should_return_user_when_user_roles_includes_authorized_roles() {

        User user = idamRequestAuthorizer.authorise(request);

        assertThat(user.getRoles()).contains(role1, role2);
    }

    @Test
    public void should_throw_missing_bearer_token_missing_exception_when_it_is_not_set() {

        when(request.getHeader(IdamRequestAuthorizer.AUTHORISATION)).thenReturn(null);

        assertThatThrownBy(() -> idamRequestAuthorizer.authorise(request))
            .isExactlyInstanceOf(BearerTokenInvalidException.class);
    }

    @Test
    public void should_throw_bearer_token_invalid_exception_when_token_has_non_alphanumerical_chars() {

        String nonAlphaToken = "Bearer invalidToken\n\r";
        when(request.getHeader(IdamRequestAuthorizer.AUTHORISATION)).thenReturn(nonAlphaToken);

        assertThatThrownBy(() -> idamRequestAuthorizer.authorise(request))
            .hasCauseExactlyInstanceOf(RuntimeException.class)
            .isExactlyInstanceOf(BearerTokenInvalidException.class);
    }

    @Test
    public void should_throw_bearer_token_invalid_exception_when_token_is_invalid() {

        String invalidToken = "Bearer invalidToken";
        when(request.getHeader(IdamRequestAuthorizer.AUTHORISATION)).thenReturn(invalidToken);
        when(idamTokenVerification.verifyTokenSignature(invalidToken)).thenReturn(false);

        assertThatThrownBy(() -> idamRequestAuthorizer.authorise(request))
            .hasCauseExactlyInstanceOf(RuntimeException.class)
            .isExactlyInstanceOf(BearerTokenInvalidException.class);
    }

    @Test
    public void should_throw_unauthorized_role_exception_when_user_unauthorized() {

        when(userDetailsProvider.getUserDetails(validToken)).thenReturn(new IdamUserDetails(
            validToken,
            "someId",
            Lists.newArrayList(),
            "someEmail",
            "someGivenName",
            "someFamilyName"
        ));

        assertThatThrownBy(() -> idamRequestAuthorizer.authorise(request))
            .isExactlyInstanceOf(UnauthorisedRoleException.class);
    }

    @Test
    public void should_throw_unauthorized_role_exception_when_user_provider_has_issue() {

        when(userDetailsProvider.getUserDetails(validToken)).thenThrow(IdentityManagerResponseException.class);

        assertThatThrownBy(() -> idamRequestAuthorizer.authorise(request))
            .isExactlyInstanceOf(UnauthorisedRoleException.class);
    }

    @Test
    public void should_return_user_when_authorized_roles_missing() {

        when(authorizedRolesExtractor.apply(request)).thenReturn(new HashSet<>());

        User user = idamRequestAuthorizer.authorise(request);

        assertThat(user.getRoles()).contains(role1, role2);
    }

}