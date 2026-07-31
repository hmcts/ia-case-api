package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CryptoUtilsTest {

    private static String generateValidBase64Key() {

        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);

        return Base64.getEncoder().encodeToString(bytes);
    }

    @Test
    void shouldCreateKey() {

        SecretKey key =
                CryptoUtils.createKey(generateValidBase64Key());

        assertNotNull(key);
        assertEquals("AES", key.getAlgorithm());
    }

    @Test
    void shouldEncryptAndDecrypt() {

        SecretKey key =
                CryptoUtils.createKey(generateValidBase64Key());

        String plaintext =
                "Highly sensitive value";

        String encrypted =
                CryptoUtils.encrypt(plaintext, key);

        String decrypted =
                CryptoUtils.decrypt(encrypted, key);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void shouldEncryptSameValueDifferentlyEachTime() {

        SecretKey key =
                CryptoUtils.createKey(generateValidBase64Key());

        String plaintext = "same text";

        String encrypted1 =
                CryptoUtils.encrypt(plaintext, key);

        String encrypted2 =
                CryptoUtils.encrypt(plaintext, key);

        assertNotEquals(encrypted1, encrypted2);
    }

    @Test
    void shouldHandleEmptyString() {

        SecretKey key =
                CryptoUtils.createKey(generateValidBase64Key());

        String encrypted =
                CryptoUtils.encrypt("", key);

        String decrypted =
                CryptoUtils.decrypt(encrypted, key);

        assertEquals("", decrypted);
    }

    @Test
    void shouldHandleUnicode() {

        SecretKey key =
                CryptoUtils.createKey(generateValidBase64Key());

        String plaintext =
                "こんにちは 🔐 Привет مرحبا";

        String encrypted =
                CryptoUtils.encrypt(plaintext, key);

        String decrypted =
                CryptoUtils.decrypt(encrypted, key);

        assertEquals(plaintext, decrypted);
    }

    @Test
    void shouldRejectNullSecret() {

        assertThrows(
                NullPointerException.class,
                () -> CryptoUtils.createKey(null));
    }

    @Test
    void shouldRejectWrongLengthKey() {

        byte[] bytes = new byte[16];

        String secret =
                Base64.getEncoder().encodeToString(bytes);

        assertThrows(
                IllegalArgumentException.class,
                () -> CryptoUtils.createKey(secret));
    }

    @Test
    void shouldRejectInvalidBase64Secret() {

        assertThrows(
                IllegalArgumentException.class,
                () -> CryptoUtils.createKey("%%%"));
    }

    @Test
    void shouldRejectNullPlaintext() {

        SecretKey key =
                CryptoUtils.createKey(generateValidBase64Key());

        assertThrows(
                NullPointerException.class,
                () -> CryptoUtils.encrypt(null, key));
    }

    @Test
    void shouldRejectNullKeyOnEncrypt() {

        assertThrows(
                NullPointerException.class,
                () -> CryptoUtils.encrypt("abc", null));
    }

    @Test
    void shouldRejectNullKeyOnDecrypt() {

        assertThrows(
                NullPointerException.class,
                () -> CryptoUtils.decrypt("abc", null));
    }

    @Test
    void shouldFailForTamperedCiphertext() {

        SecretKey key =
                CryptoUtils.createKey(generateValidBase64Key());

        String encrypted =
                CryptoUtils.encrypt("secret", key);

        char replacement =
                encrypted.charAt(encrypted.length() - 1) == 'A'
                        ? 'B'
                        : 'A';

        String tampered =
                encrypted.substring(0, encrypted.length() - 1)
                        + replacement;

        assertThrows(
                CryptoException.class,
                () -> CryptoUtils.decrypt(tampered, key));
    }

    @Test
    void shouldFailWithWrongKey() {

        SecretKey key1 =
                CryptoUtils.createKey(generateValidBase64Key());

        SecretKey key2 =
                CryptoUtils.createKey(generateValidBase64Key());

        String encrypted =
                CryptoUtils.encrypt("secret", key1);

        assertThrows(
                CryptoException.class,
                () -> CryptoUtils.decrypt(encrypted, key2));
    }

    @Test
    void shouldRejectInvalidPayload() {

        String invalid =
                Base64.getEncoder()
                        .encodeToString(new byte[5]);

        SecretKey key =
                CryptoUtils.createKey(generateValidBase64Key());

        assertThrows(
                CryptoException.class,
                () -> CryptoUtils.decrypt(invalid, key));
    }
}