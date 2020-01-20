package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.jwk.AsymmetricJWK;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.SecretJWK;
import com.nimbusds.jose.proc.JWSVerifierFactory;
import com.nimbusds.jwt.SignedJWT;
import java.security.Key;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.auth.checker.core.exceptions.BearerTokenInvalidException;

@Slf4j
public class IdamTokenVerification {

    private final String baseUrl;
    private final String jwksUri;
    private final RestTemplate restTemplate;
    private final JWSVerifierFactory jwsVerifierFactory;

    public IdamTokenVerification(String baseUrl, String jwksUri, RestTemplate restTemplate, JWSVerifierFactory jwsVerifierFactory) {
        this.baseUrl = baseUrl;
        this.jwksUri = jwksUri;
        this.restTemplate = restTemplate;
        this.jwsVerifierFactory = jwsVerifierFactory;
    }

    public boolean verifyTokenSignature(String token) {

        try {
            String tokenToCheck = StringUtils.replace(token, "Bearer ", "");
            SignedJWT signedJwt = SignedJWT.parse(tokenToCheck);

            JWKSet jsonWebKeySet = loadJsonWebKeySet();

            JWSHeader jwsHeader = signedJwt.getHeader();
            Key key = findKeyById(jsonWebKeySet, jwsHeader.getKeyID());

            JWSVerifier jwsVerifier = jwsVerifierFactory.createJWSVerifier(jwsHeader, key);

            return signedJwt.verify(jwsVerifier);
        } catch (Exception e) {
            log.error("Token validation error", e);

            return false;
        }
    }

    private JWKSet loadJsonWebKeySet() {

        try {
            return JWKSet.parse(
                restTemplate
                    .exchange(
                        baseUrl + jwksUri,
                        HttpMethod.GET,
                        HttpEntity.EMPTY,
                        String.class
                    )
                    .getBody()
            );
        } catch (Exception e) {
            throw new BearerTokenInvalidException(e);
        }
    }

    private Key findKeyById(JWKSet jsonWebKeySet, String keyId) {

        try {
            JWK jsonWebKey = jsonWebKeySet.getKeyByKeyId(keyId);
            if (jsonWebKey == null) {
                throw new RuntimeException("JWK does not exist in the key set");
            }
            if (jsonWebKey instanceof SecretJWK) {
                return ((SecretJWK) jsonWebKey).toSecretKey();
            }
            if (jsonWebKey instanceof AsymmetricJWK) {
                return ((AsymmetricJWK) jsonWebKey).toPublicKey();
            }
            throw new RuntimeException("Unsupported JWK " + jsonWebKey.getClass().getName());
        } catch (Exception e) {
            throw new BearerTokenInvalidException(e);
        }
    }

}
