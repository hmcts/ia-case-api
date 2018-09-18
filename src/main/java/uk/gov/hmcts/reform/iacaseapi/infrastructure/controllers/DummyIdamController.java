package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.slf4j.LoggerFactory.getLogger;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.*;

@RestController
public class DummyIdamController {

    private static final org.slf4j.Logger LOG = getLogger(DummyIdamController.class);

    private final ObjectMapper mapper;

    public DummyIdamController(
        @Autowired ObjectMapper mapper
    ) {
        this.mapper = mapper;
    }

    @RequestMapping(
        method = {
            RequestMethod.DELETE,
            RequestMethod.GET,
            RequestMethod.POST,
            RequestMethod.PUT
        },
        value = "/logout"
    )
    public ResponseEntity logout() {
        HttpHeaders headers = new HttpHeaders();
        headers.clear();
        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/login")
    public String login(
        @RequestParam("redirect_uri") String redirectUri
    ) throws IOException {
        URL url = Resources.getResource("html/login.html");
        return Resources.toString(url, Charsets.UTF_8);
    }

    @PostMapping(value = "/login")
    public ResponseEntity loginSelect(
        @RequestParam("code") String code,
        @RequestParam("redirect_uri") String redirectUri
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUri + "?code=" + code));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @PostMapping(
        value = "/oauth2/token",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String token(
        @RequestParam("code") String code
    ) {
        String accessToken = "";

        // Import Script
        if ("import".equals(code)) {
            accessToken = createAccessToken(
                1,
                "Import",
                "Script",
                "ccd-import"
            );
        }

        // Legal Rep
        if ("legal".equals(code)) {
            accessToken = createAccessToken(
                2,
                "Legal",
                "Rep",
                "caseworker",
                "caseworker-sscs",
                "caseworker-sscs-anonymouscitizen"
            );
        }

        // Law Firm A
        if ("law-firm-a".equals(code)) {
            accessToken = createAccessToken(
                3,
                "Law Firm",
                "A",
                "caseworker",
                "caseworker-sscs",
                "caseworker-sscs-anonymouscitizen",
                "caseworker-sscs-law-firm-a-solicitor"
            );
        }

        // Law Firm B
        if ("law-firm-b".equals(code)) {
            accessToken = createAccessToken(
                4,
                "Law Firm",
                "B",
                "caseworker",
                "caseworker-sscs",
                "caseworker-sscs-anonymouscitizen",
                "caseworker-sscs-law-firm-b-solicitor"
            );
        }

        // Case Officer
        if ("case".equals(code)) {
            accessToken = createAccessToken(
                5,
                "Case",
                "Officer",
                "caseworker",
                "caseworker-sscs",
                "caseworker-sscs-callagent"
            );
        }

        // Super User
        if ("super".equals(code)) {
            accessToken = createAccessToken(
                6,
                "Super",
                "user",
                "caseworker",
                "caseworker-sscs",
                "caseworker-sscs-anonymouscitizen",
                "caseworker-sscs-callagent",
                "caseworker-sscs-judge",
                "caseworker-sscs-systemupdate"
            );
        }

        if (accessToken.isEmpty()) {
            throw new RuntimeException("User code not recognised");
        }

        return
            "{"
            + "\"access_token\": \"" + accessToken + "\","
            + "\"token_type\":   \"Bearer\","
            + "\"expires_in\":   " + Instant.now().plus(1, DAYS).getEpochSecond()
            + "}";
    }

    @GetMapping(
        value = "/details",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> details(
        @RequestHeader String authorization
    ) {
        Map<String, String> token = decodeAccessToken(authorization);

        String details = "";

        // Import Script
        if (token.getOrDefault("forename", "").equals("Import")) {

            details = createUserDetails(
                1,
                "Import",
                "Script",
                "import-script@example.com",
                "ccd-import"
            );
        }

        // Legal Rep
        if (token.getOrDefault("surname", "").equals("Rep")) {

            details = createUserDetails(
                2,
                "Legal",
                "Rep",
                "ia-legal-rep@example.com",
                "caseworker",
                "caseworker-sscs",
                "caseworker-sscs-anonymouscitizen"
            );
        }

        // Law Firm A
        if (token.getOrDefault("forename", "").equals("Law Firm")
            && token.getOrDefault("surname", "").equals("A")) {

            details = createUserDetails(
                2,
                "Law Firm",
                "A",
                "ia-law-firm-a@example.com",
                "caseworker",
                "caseworker-sscs",
                "caseworker-sscs-anonymouscitizen",
                "caseworker-sscs-law-firm-a-solicitor"
            );
        }

        // Law Firm B
        if (token.getOrDefault("forename", "").equals("Law Firm")
            && token.getOrDefault("surname", "").equals("B")) {

            details = createUserDetails(
                3,
                "Law Firm",
                "B",
                "ia-law-firm-b@example.com",
                "caseworker",
                "caseworker-sscs",
                "caseworker-sscs-anonymouscitizen",
                "caseworker-sscs-law-firm-b-solicitor"
            );
        }

        // Case Officer
        if (token.getOrDefault("forename", "").equals("Case")) {

            details = createUserDetails(
                4,
                "Case",
                "Officer",
                "ia-case-officer@example.com",
                "caseworker",
                "caseworker-sscs",
                "caseworker-sscs-callagent"
            );
        }

        // Super User
        if (token.getOrDefault("forename", "").equals("Super")) {

            details = createUserDetails(
                5,
                "Super",
                "user",
                "ia-super-user@example.com",
                "caseworker",
                "caseworker-sscs",
                "caseworker-sscs-anonymouscitizen",
                "caseworker-sscs-callagent",
                "caseworker-sscs-judge",
                "caseworker-sscs-systemupdate"
            );
        }

        if (details.isEmpty()) {
            LOG.warn("Authorization Token Issue: user not recognised");
            HttpHeaders headers = new HttpHeaders();
            headers.clear();
            return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(details, HttpStatus.OK);
        }
    }

    private String createAccessToken(
        int id,
        String forename,
        String surname,
        String... roles
    ) {
        return JWT.create()
            .withSubject(String.valueOf(id))
            .withExpiresAt(Date.from(Instant.now()))
            .withIssuedAt(Date.from(Instant.now().plus(1, DAYS)))
            .withClaim("jti", (int) Math.ceil(Math.random() * 999999))
            .withClaim("id", String.valueOf(id))
            .withClaim("type", "ACCESS")
            .withClaim("forename", forename)
            .withClaim("surname", surname)
            .withClaim("loa", 1)
            .withClaim("data", StringUtils.join(roles, "\",\""))
            .withClaim("group", "caseworker")
            .withClaim("default-service", "CCD")
            .withClaim("default-url", "https://localhost")
            .sign(Algorithm.HMAC256("foobar"));
    }

    private String createUserDetails(
        int id,
        String forename,
        String surname,
        String email,
        String... roles
    ) {
        return
            "{"
            + "\"id\":              " + id + ","
            + "\"forename\":      \"" + forename + "\","
            + "\"surname\":       \"" + surname + "\","
            + "\"email\":         \"" + email + "\","
            + "\"accountStatus\": \"active\","
            + "\"roles\":         [\"" + StringUtils.join(roles, "\",\"") + "\"]"
            + "}";
    }

    private Map<String, String> decodeAccessToken(
        String accessToken
    ) {
        try {

            DecodedJWT jwt = JWT.decode(
                accessToken.replaceFirst("^Bearer\\s+", "")
            );

            String accessTokenClaims = new String(
                Base64Utils
                    .decodeFromString(jwt.getPayload())
            );

            try {

                return mapper.readValue(
                    accessTokenClaims,
                    new TypeReference<Map<String, String>>() {
                    }
                );

            } catch (IOException e) {
                throw new RuntimeException("Authorization Token claims cannot be deserialized", e);
            }

        } catch (JWTDecodeException e) {
            throw new JWTDecodeException("Authorization Token cannot be decoded using JWT format", e);
        }
    }
}
