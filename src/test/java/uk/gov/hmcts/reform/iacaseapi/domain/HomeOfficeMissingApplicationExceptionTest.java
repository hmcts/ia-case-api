package uk.gov.hmcts.reform.iacaseapi.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

class HomeOfficeMissingApplicationExceptionTest {

    @Test
    void shouldStoreMessageAndHttpStatus() {
        int status = 404;
        String message = "Application not found";

        HomeOfficeMissingApplicationException exception =
            new HomeOfficeMissingApplicationException(status, message);

        Assertions.assertEquals(message, exception.getMessage());
        Assertions.assertEquals(status, exception.getHttpStatus());
    }

    @Test
    void shouldBeInstanceOfRuntimeException() {
        HomeOfficeMissingApplicationException exception =
            new HomeOfficeMissingApplicationException(400, "Bad request");

        Assertions.assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void shouldThrowAndContainCorrectValues() {
        int status = 500;
        String message = "Internal error";

        HomeOfficeMissingApplicationException thrown =
            Assertions.assertThrows(
                HomeOfficeMissingApplicationException.class,
                () -> {
                    throw new HomeOfficeMissingApplicationException(status, message);
                }
            );

        Assertions.assertEquals(status, thrown.getHttpStatus());
        Assertions.assertEquals(message, thrown.getMessage());
    }
}
