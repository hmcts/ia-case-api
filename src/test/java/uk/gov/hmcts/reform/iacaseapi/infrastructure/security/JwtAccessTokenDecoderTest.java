package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.Test;

public class JwtAccessTokenDecoderTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtAccessTokenDecoder underTest = new JwtAccessTokenDecoder(objectMapper);

    /* Pre encoded JWT
     {
     "alg": "HS256",
     "typ": "JWT"
     }
     {
     "sub": "1234567890",
     "name": "John Doe",
     "iat": 1516239022
     }
     */
    private static String wellFormedJwtToken() {
        return "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }

    /* Pre encoded JWT
     {
        unreadable header
     }
     {
     "sub": "1234567890",
     "name": "John Doe",
     "iat": 1516239022
     }
     */
    private static String badlyFormedJwtToke() {
        return "Bearer eyJhbGciOiJIUzI1.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }

    @Test
    public void decodes_jwt_access_token() {

        assertThatThrownBy(() -> underTest.decode(badlyFormedJwtToke()))
                .isExactlyInstanceOf(JWTDecodeException.class)
                .hasMessage("Access Token is not in JWT format");
    }

    @Test
    public void handles_badly_formed_jwt_access_token() {

        Map<String, String> claims = underTest.decode(wellFormedJwtToken());

        assertThat(claims.get("sub")).isEqualTo("1234567890");
        assertThat(claims.get("name")).isEqualTo("John Doe");
        assertThat(claims.get("iat")).isEqualTo("1516239022");
    }



}