package uk.gov.hmcts.reform.iacaseapi.domain;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import org.junit.Test;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserRole;

public class UserDetailsProviderTest {

    UserDetails userDetails = spy(UserDetails.class);
    UserDetailsProvider userDetailsProvider = spy(UserDetailsProvider.class);

    @Test
    public void should_return_logged_in_user_role() {

        when(userDetails.getRoles()).thenReturn(Arrays.asList(UserRole.CASE_OFFICER.toString()));
        when(userDetails.getForename()).thenReturn("ForeName");
        when(userDetails.getSurname()).thenReturn("Surname");
        when(userDetails.getEmailAddress()).thenReturn("email@domain.com");
        when(userDetails.getId()).thenReturn("1");
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);

        assertThat(userDetailsProvider.getLoggedInUserRole()).isEqualTo(UserRole.CASE_OFFICER);
    }

    @Test
    public void should_throw_no_valid_role_exists() {

        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);

        assertThatThrownBy(() -> userDetailsProvider.getLoggedInUserRole())
            .isExactlyInstanceOf(IllegalStateException.class)
            .hasMessage("No valid user role is present.");
    }
}
