package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

public class ProfessionalUserTest {

    private final String userIdentifier = "someUserId1";
    private final String firstName = "someFirstName";
    private final String lastName = "someLastName";
    private final String email = "john.doe@example.com";
    private final String idamStatus = "ACTIVE";
    private final String idamStatusCode = "someCode";
    private final String idamMessage = "someMessage";
    private final String userRole = "test-user-role";

    List<String> roles = new ArrayList<>(List.of(userRole));

    private ProfessionalUser professionalUser = new ProfessionalUser(
        userIdentifier,
        firstName,
        lastName,
        email,
        roles,
        idamStatus,
        idamStatusCode,
        idamMessage
    );

    @Test
    public void should_hold_onto_values() {

        assertEquals(userIdentifier, professionalUser.getUserIdentifier());
        assertEquals(firstName, professionalUser.getFirstName());
        assertEquals(lastName, professionalUser.getLastName());
        assertEquals(email, professionalUser.getEmail());
        assertThat(professionalUser.getRoles()).hasSize(1);
        assertThat(professionalUser.getRoles().get(0)).isEqualTo(userRole);
        assertEquals(idamStatus, professionalUser.getIdamStatus());
        assertEquals(idamStatusCode, professionalUser.getIdamStatusCode());
        assertEquals(idamMessage, professionalUser.getIdamMessage());

    }

}
