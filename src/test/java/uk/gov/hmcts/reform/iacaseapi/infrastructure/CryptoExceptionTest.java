package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertAll;

class CryptoExceptionTest {

    @Test
    void shouldPreserveMessage() {

        CryptoException exception =
                new CryptoException("Encryption failed", null);

        assertEquals(
                "Encryption failed",
                exception.getMessage());
    }

    @Test
    void shouldPreserveCause() {

        RuntimeException cause =
                new RuntimeException("Root cause");

        CryptoException exception =
                new CryptoException(
                        "Encryption failed",
                        cause);

        assertSame(cause, exception.getCause());
    }

    @Test
    void shouldBeRuntimeException() {

        CryptoException exception =
                new CryptoException(
                        "Encryption failed",
                        null);

        assertInstanceOf(
                RuntimeException.class,
                exception);
    }

    @Test
    void shouldPreserveMessageAndCause() {

        IllegalArgumentException cause =
                new IllegalArgumentException("Bad input");

        CryptoException exception =
                new CryptoException(
                        "Decryption failed",
                        cause);

        assertAll(
                () -> assertEquals(
                        "Decryption failed",
                        exception.getMessage()),
                () -> assertSame(
                        cause,
                        exception.getCause())
        );
    }
}