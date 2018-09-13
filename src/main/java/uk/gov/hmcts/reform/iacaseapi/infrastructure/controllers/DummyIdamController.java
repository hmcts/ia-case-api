package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers;

import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class DummyIdamController {

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
    ) {
        return ""
               + "<html>"
               + "<body>"
               + "<hr/>"
               + "<form method=post><input name=code type=hidden value=legal><p><button>Legal Representative</button></p></form>"
               + "<form method=post><input name=code type=hidden value=officer><p><button>Case Officer</button></p></form>"
               + "<hr/>"
               + "<form method=post><input name=code type=hidden value=super><p><button>Assume Both Roles</button></p></form>"
               + "<hr/>"
               + "</body>"
               + "</html>";
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

        if ("legal".equals(code)) {
            accessToken = getLegalAccessToken();
        }

        if ("officer".equals(code)) {
            accessToken = getOfficerAccessToken();
        }

        if ("super".equals(code)) {
            accessToken = getSuperAccessToken();
        }

        return
            "{"
            + "\"expires_in\":   31536000," // 1 year
            + "\"token_type\":   \"Bearer\","
            + "\"access_token\": \"" + accessToken + "\""
            + "}";
    }

    @GetMapping(
        value = "/details",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public String details(
        @RequestHeader String authorization
    ) {
        // Import Script
        if (authorization.contains("MgXM7p7FjKoibcciPdq788wQ2jyq9gd7Ix6QiwoHcuY")) {
            String details = createUserResponse(
                1,
                "Import",
                "Script",
                "import-script@example.com",
                "ccd-import"
            );

            return details;
        }

        // Legal Rep
        if (authorization.contains("kf991vGFbU")) {

            return createUserResponse(
                2,
                "Legal",
                "Rep",
                "ia-legal-rep@example.com",
                "caseworker",
                "caseworker-sscs",
                "caseworker-sscs-anonymouscitizen"
            );
        }

        // Case Officer
        if (authorization.contains("Hj7wJj5ypENb1QBIeAyA9LW_hySJ6prU_jVvE")) {

            return createUserResponse(
                3,
                "Case",
                "Officer",
                "ia-legal-rep@example.com",
                "caseworker",
                "caseworker-sscs",
                "caseworker-sscs-callagent"
            );
        }

        // Super User
        if (authorization.contains("ZP4BoV8HxOxpguemeCRyxzKnf2lfDdmoY")) {

            return createUserResponse(
                4,
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

        return "{}";
    }

    private String createUserResponse(
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

    private String getLegalAccessToken() {
        return "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxMTExMTExMTExMTExMTExMTEx"
               + "MTExMTExMSIsInN1YiI6IjExIiwiaWF0IjoxNTM2Nzg2ODU4LCJleHA"
               + "iOjI1MzY4MTU2NTgsImRhdGEiOiJjYXNld29ya2VyLXNzY3MsY2FzZX"
               + "dvcmtlci1zc2NzLWFub255bW91c2NpdGl6ZW4sY2FzZXdvcmtlcixjY"
               + "XNld29ya2VyLXNzY3MtbG9hMSxjYXNld29ya2VyLXNzY3MtYW5vbnlt"
               + "b3VzY2l0aXplbi1sb2ExLGNhc2V3b3JrZXItbG9hMSIsInR5cGUiOiJ"
               + "BQ0NFU1MiLCJpZCI6IjUwIiwiZm9yZW5hbWUiOiJMZWdhbCIsInN1cm"
               + "5hbWUiOiJSZXAiLCJkZWZhdWx0LXNlcnZpY2UiOiJDQ0QiLCJsb2EiO"
               + "jEsImRlZmF1bHQtdXJsIjoiaHR0cHM6Ly9sb2NhbGhvc3QiLCJncm91"
               + "cCI6ImNhc2V3b3JrZXIifQ.5_58k_xeCX4aRtgPK2ksjQrI5fcDKdJV"
               + "-kf991vGFbU";
    }

    private String getOfficerAccessToken() {
        return "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxMTExMTExMTExMTExMTExMTEx"
               + "MTExMTExMSIsInN1YiI6IjExIiwiaWF0IjoxNTM2Nzg2ODU4LCJleHA"
               + "iOjI1MzY4MTU2NTgsImRhdGEiOiJjYXNld29ya2VyLXNzY3MsY2FzZX"
               + "dvcmtlci1zc2NzLWNhbGxhZ2VudCxjYXNld29ya2VyLGNhc2V3b3JrZ"
               + "XItc3Njcy1sb2ExLGNhc2V3b3JrZXItc3Njcy1jYWxsYWdlbnQtbG9h"
               + "MSxjYXNld29ya2VyLWxvYTEiLCJ0eXBlIjoiQUNDRVNTIiwiaWQiOiI"
               + "3NSIsImZvcmVuYW1lIjoiQ2FzZSIsInN1cm5hbWUiOiJPZmZpY2VyIi"
               + "wiZGVmYXVsdC1zZXJ2aWNlIjoiQ0NEIiwibG9hIjoxLCJkZWZhdWx0L"
               + "XVybCI6Imh0dHBzOi8vbG9jYWxob3N0IiwiZ3JvdXAiOiJjYXNld29y"
               + "a2VyIn0.RWSn_-Hj7wJj5ypENb1QBIeAyA9LW_hySJ6prU_jVvE";
    }

    private String getSuperAccessToken() {
        return "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxMTExMTExMTE"
               + "xMTExMTExMTExMTExMTExMSIsInN1YiI6IjExIiwiaWF0IjoxNTM2N"
               + "zg2ODU4LCJleHAiOjI1MzY4MTU2NTgsImRhdGEiOiJjYXNld29ya2V"
               + "yLXNzY3MsY2FzZXdvcmtlci1zc2NzLWFub255bW91c2NpdGl6ZW4sY"
               + "2FzZXdvcmtlci1zc2NzLWNhbGxhZ2VudCxjYXNld29ya2VyLXNzY3M"
               + "tanVkZ2UsY2FzZXdvcmtlci1zc2NzLXN5c3RlbXVwZGF0ZSxjYXNld"
               + "29ya2VyLGNhc2V3b3JrZXItc3Njcy1sb2ExLGNhc2V3b3JrZXItc3N"
               + "jcy1hbm9ueW1vdXNjaXRpemVuLWxvYTEsY2FzZXdvcmtlci1zc2NzL"
               + "WNhbGxhZ2VudC1sb2ExLGNhc2V3b3JrZXItc3Njcy1qdWRnZS1sb2E"
               + "xLGNhc2V3b3JrZXItc3Njcy1zeXN0ZW11cGRhdGUtbG9hMSxjYXNld"
               + "29ya2VyLWxvYTEiLCJ0eXBlIjoiQUNDRVNTIiwiaWQiOiIyNSIsImZ"
               + "vcmVuYW1lIjoiTGVnYWwiLCJzdXJuYW1lIjoiUmVwIiwiZGVmYXVsd"
               + "C1zZXJ2aWNlIjoiQ0NEIiwibG9hIjoxLCJkZWZhdWx0LXVybCI6Imh"
               + "0dHBzOi8vbG9jYWxob3N0IiwiZ3JvdXAiOiJjYXNld29ya2VyIn0.L"
               + "SM0fdBKH-ZP4BoV8HxOxpguemeCRyxzKnf2lfDdmoY";
    }
}
