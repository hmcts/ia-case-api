package uk.gov.hmcts.reform.iacaseapi.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

@Component
public class JwtAccessTokenDecoder implements AccessTokenDecoder {

    private final ObjectMapper mapper;

    public JwtAccessTokenDecoder(
            @Autowired ObjectMapper mapper
    ) {
        this.mapper = mapper;
    }

    public Map<String, String> decode(
            String accessToken
    ) {
        try {

            DecodedJWT jwt = JWT.decode(
                    accessToken.replaceFirst("^Bearer\\s+", "")
            );

            String accessTokenClaims = new String(
                    Base64Utils.decodeFromString(jwt.getPayload())
            );

            return mapper.readValue(
                    accessTokenClaims,
                    new TypeReference<Map<String, String>>() {
                    }
            );


        } catch (JWTDecodeException | IOException e) {
            throw new JWTDecodeException("Access Token is not in JWT format", e);
        }
    }
}
