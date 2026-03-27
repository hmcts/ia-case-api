package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
public class AesEncryptingRedisSerializer<T> implements RedisSerializer<T> {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final RedisSerializer<T> delegate;
    private final SecretKeySpec secretKey;


    public AesEncryptingRedisSerializer(RedisSerializer<T> delegate, String base64Key) {
        this.delegate = delegate;
        byte[] keyBytes = Base64.getDecoder().decode(base64Key);
        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    @Override
    public byte[] serialize(T value) throws SerializationException {

        log.info("VALUE TO SERIALIZE: " + value);

        if (value == null) {
            return null;
        }

        try {
            byte[] plainText = delegate.serialize(value);

            log.info("plainText: " + Arrays.toString(plainText));

            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] ciphertext = cipher.doFinal(plainText);

            // Prepend IV to ciphertext: [IV (12 bytes)][ciphertext]
            byte[] result = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);

            log.info("Token serialized and encrypted, bytes length: {}", result.length);
            return result;

        } catch (Exception e) {
            throw new SerializationException("Encryption failed", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        log.info("VALUE TO DESERIALIZE: " + Arrays.toString(bytes));

        if (bytes == null) {
            return null;
        }

        try {
            byte[] iv = Arrays.copyOfRange(bytes, 0, GCM_IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(bytes, GCM_IV_LENGTH, bytes.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] plaintext = cipher.doFinal(ciphertext);
            log.info("plaintext: " + Arrays.toString(plaintext));
            return delegate.deserialize(plaintext);
        } catch (Exception e) {
            throw new SerializationException("Decryption failed", e);
        }
    }
}
