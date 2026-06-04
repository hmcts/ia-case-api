package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;

public final class CryptoUtils {

    private static final String AES = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    private static final int KEY_LENGTH_BYTES = 32;
    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CryptoUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Constructs an AES key from a Base64 encoded 32-byte secret
     * retrieved from Azure Key Vault.
     */
    public static SecretKey createKey(String base64Secret) {

        Objects.requireNonNull(base64Secret, "base64Secret");

        byte[] keyBytes = Base64.getDecoder().decode(base64Secret);

        if (keyBytes.length != KEY_LENGTH_BYTES) {
            throw new IllegalArgumentException(
                    "Expected a 32-byte AES key but found "
                            + keyBytes.length + " bytes");
        }

        return new SecretKeySpec(keyBytes, AES);
    }

    public static String encrypt(String plaintext, SecretKey key) {

        Objects.requireNonNull(plaintext, "plaintext");
        Objects.requireNonNull(key, "key");

        try {

            byte[] iv = new byte[IV_LENGTH_BYTES];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    key,
                    new GCMParameterSpec(TAG_LENGTH_BITS, iv));

            byte[] ciphertext =
                    cipher.doFinal(
                            plaintext.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer =
                    ByteBuffer.allocate(iv.length + ciphertext.length);

            buffer.put(iv);
            buffer.put(ciphertext);

            return Base64.getEncoder()
                    .encodeToString(buffer.array());

        } catch (Exception e) {
            throw new CryptoException("Encryption failed", e);
        }
    }

    public static String decrypt(String encryptedValue, SecretKey key) {

        Objects.requireNonNull(encryptedValue, "encryptedValue");
        Objects.requireNonNull(key, "key");

        try {

            byte[] combined =
                    Base64.getDecoder().decode(encryptedValue);

            if (combined.length <= IV_LENGTH_BYTES) {
                throw new IllegalArgumentException(
                        "Encrypted payload is invalid");
            }

            ByteBuffer buffer = ByteBuffer.wrap(combined);

            byte[] iv = new byte[IV_LENGTH_BYTES];
            buffer.get(iv);

            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    new GCMParameterSpec(TAG_LENGTH_BITS, iv));

            byte[] plaintext =
                    cipher.doFinal(ciphertext);

            return new String(
                    plaintext,
                    StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new CryptoException("Decryption failed", e);
        }
    }
}