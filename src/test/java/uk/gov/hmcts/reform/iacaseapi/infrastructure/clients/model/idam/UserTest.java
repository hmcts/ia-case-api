package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class UserTest {
    private final String email = "email@test.com";
    private final String idamId = "123";
    private final String forename = "John";
    private final String surname = "Doe";
    private final User user =
        new User(idamId, forename, surname, email, true, null);

    @Test
    void toRevokeAccessDlString_citizen_() {
        String dlStringNonNlr = user.toRevokeAccessDlString("124");
        assertTrue(dlStringNonNlr.contains(forename));
        assertTrue(dlStringNonNlr.contains(surname));
        assertTrue(dlStringNonNlr.contains(email));
        assertTrue(dlStringNonNlr.contains("Citizen"));
        String dlStringNlr = user.toRevokeAccessDlString(idamId);
        assertTrue(dlStringNlr.contains(forename));
        assertTrue(dlStringNlr.contains(surname));
        assertTrue(dlStringNlr.contains(email));
        assertTrue(dlStringNlr.contains("Non Legal Rep"));
    }

    @Test
    void toValueId() {
        String valueId = user.toValueId();
        assertTrue(valueId.contains(idamId));
        assertTrue(valueId.contains(email));
        assertEquals(idamId + ":" + email, user.toValueId());
    }
}