package uk.gov.hmcts.reform.bailcaseapi.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.idam.UserInfo;

import java.util.Base64;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class AesEncryptingRedisSerializerTest {

    private static final String VALID_KEY_256 = Base64.getEncoder()
        .encodeToString("01234567890123456789012345678901".getBytes());

    private AesEncryptingRedisSerializer<String> serializer;
    private Jackson2JsonRedisSerializer<String> delegateSerializer;

    @BeforeEach
    void setUp() {
        delegateSerializer = new Jackson2JsonRedisSerializer<>(String.class);
        serializer = new AesEncryptingRedisSerializer<>(delegateSerializer, VALID_KEY_256);
    }

    @Test
    void serialize_null_returnsNull() {
        assertThat(serializer.serialize(null)).isNull();
    }

    @Test
    void deserialize_null_returnsNull() {
        assertThat(serializer.deserialize(null)).isNull();
    }

    @Test
    void roundTrip_plainString() {
        String original = "Bearer eyJhbGciOiJSUzI1NiJ9.sometoken";

        byte[] serialized = serializer.serialize(original);
        String deserialized = serializer.deserialize(serialized);

        assertThat(deserialized).isEqualTo(original);
    }

    @Test
    void roundTrip_pojoDelegate() {
        Jackson2JsonRedisSerializer<UserInfo> userInfoDelegate =
            new Jackson2JsonRedisSerializer<>(UserInfo.class);
        AesEncryptingRedisSerializer<UserInfo> userInfoSerializer =
            new AesEncryptingRedisSerializer<>(userInfoDelegate, VALID_KEY_256);

        UserInfo userInfo = new UserInfo();

        byte[] serialized = userInfoSerializer.serialize(userInfo);
        UserInfo deserialized = userInfoSerializer.deserialize(serialized);

        assertThat(deserialized.getEmail()).isEqualTo(userInfo.getEmail());
        assertThat(deserialized.getName()).isEqualTo(userInfo.getName());
    }

    @Test
    void serialize_doesNotContainPlaintext() {
        String token = "Bearer token";

        byte[] encrypted = serializer.serialize(token);

        assertThat(new String(encrypted)).doesNotContain("token");
    }

    @Test
    void serialize_sameInputProducesDifferentCiphertexts() {
        String token = "Bearer sometoken";

        byte[] first = serializer.serialize(token);
        byte[] second = serializer.serialize(token);

        // Different encrypted text, even for the same token
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void differentKeys_cannotDecryptEachOther() {
        String keyA = Base64.getEncoder().encodeToString("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA".getBytes());
        String keyB = Base64.getEncoder().encodeToString("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB".getBytes());

        AesEncryptingRedisSerializer<String> serializerA =
            new AesEncryptingRedisSerializer<>(delegateSerializer, keyA);
        AesEncryptingRedisSerializer<String> serializerB =
            new AesEncryptingRedisSerializer<>(delegateSerializer, keyB);

        byte[] encryptedWithA = serializerA.serialize("Bearer sometoken");

        // only key A can decrypt
        assertThatThrownBy(() -> serializerB.deserialize(encryptedWithA))
            .isInstanceOf(SerializationException.class);
    }

}
