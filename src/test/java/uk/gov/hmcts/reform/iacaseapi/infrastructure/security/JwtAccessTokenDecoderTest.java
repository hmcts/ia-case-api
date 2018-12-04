package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class JwtAccessTokenDecoderTest {

    @Mock private ObjectMapper objectMapper;

    private JwtAccessTokenDecoder jwtAccessTokenDecoder;

    private final String testToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1"
                                     + "NiJ9.eyJzdWIiOiIxNiIsIm5hbWUiOiJ"
                                     + "UZXN0IiwianRpIjoiMTIzNCIsImlhdCI"
                                     + "6MTUyNjkyOTk1MiwiZXhwIjoxNTI2OTM"
                                     + "zNTg5fQ.lZwrWNjG-y1Olo1qWocKIuq3"
                                     + "_fdffVF8BTcR5l87FTg";

    @Before
    public void setUp() {
        jwtAccessTokenDecoder = new JwtAccessTokenDecoder(objectMapper);
    }

    @Test
    public void can_decode_token_and_return_claims() throws IOException {

        String serializedClaimsInToken =
            "{\"sub\":\"16\",\"name\":\"Test\",\"jti\":\"1234\",\"iat\":1526929952,\"exp\":1526933589}";

        Map<String, String> deserializedClaims = mock(Map.class);

        doReturn(deserializedClaims)
            .when(objectMapper)
            .readValue(
                eq(serializedClaimsInToken),
                isA(TypeReference.class)
            );

        Map<String, String> actualClaims = jwtAccessTokenDecoder.decode(testToken);

        verify(objectMapper, times(1)).readValue(
            eq(serializedClaimsInToken),
            isA(TypeReference.class)
        );

        assertSame(actualClaims, deserializedClaims);
    }

    @Test
    public void can_decode_token_with_bearer_marker_and_return_claims() throws IOException {

        String serializedClaimsInToken =
            "{\"sub\":\"16\",\"name\":\"Test\",\"jti\":\"1234\",\"iat\":1526929952,\"exp\":1526933589}";

        Map<String, String> deserializedClaims = mock(Map.class);

        doReturn(deserializedClaims)
            .when(objectMapper)
            .readValue(
                eq(serializedClaimsInToken),
                isA(TypeReference.class)
            );

        Map<String, String> actualClaims = jwtAccessTokenDecoder.decode("Bearer " + testToken);

        verify(objectMapper, times(1)).readValue(
            eq(serializedClaimsInToken),
            isA(TypeReference.class)
        );

        assertSame(actualClaims, deserializedClaims);
    }

    @Test
    public void wraps_decode_exceptions() throws IOException {

        doThrow(JWTDecodeException.class)
            .when(objectMapper)
            .readValue(
                isA(String.class),
                isA(TypeReference.class)
            );

        assertThatThrownBy(() -> jwtAccessTokenDecoder.decode(testToken))
            .hasMessage("Access Token cannot be decoded")
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void wraps_deserialization_exceptions() throws IOException {

        doThrow(IOException.class)
            .when(objectMapper)
            .readValue(
                isA(String.class),
                isA(TypeReference.class)
            );

        assertThatThrownBy(() -> jwtAccessTokenDecoder.decode(testToken))
            .hasMessage("Access Token claims cannot be deserialized")
            .isExactlyInstanceOf(IllegalArgumentException.class);
    }
}
