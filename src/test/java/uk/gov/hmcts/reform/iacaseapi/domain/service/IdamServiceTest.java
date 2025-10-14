package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.Token;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;

@ExtendWith(MockitoExtension.class)
class IdamServiceTest {

    public static final String SOME_SYSTEM_USER = "some system user";
    public static final String SYSTEM_USER_PASS = "some system user password";
    public static final String REDIRECT_URL = "some redirect url";
    public static final String SCOPE = "some scope";
    public static final String CLIENT_ID = "some client id";
    public static final String CLIENT_SECRET = "some client secret";

    @Mock
    private IdamApi idamApi;
    @Mock
    private RoleAssignmentService roleAssignmentService;

    private IdamService idamService;

    @BeforeEach
    public void setUp() {

        idamService = new IdamService(
            SOME_SYSTEM_USER,
            SYSTEM_USER_PASS,
            REDIRECT_URL,
            SCOPE,
            CLIENT_ID,
            CLIENT_SECRET,
            idamApi,
            roleAssignmentService
        );
    }

    @Test
    void getUserToken() {
        when(idamApi.token(anyMap())).thenReturn(new Token("some user token", SCOPE));

        String actual = idamService.getServiceUserToken();

        assertThat(actual).isEqualTo("Bearer some user token");

        Map<String, String> expectedIdamApiParameter = new ConcurrentHashMap<>();
        expectedIdamApiParameter.put("grant_type", "password");
        expectedIdamApiParameter.put("redirect_uri", REDIRECT_URL);
        expectedIdamApiParameter.put("client_id", CLIENT_ID);
        expectedIdamApiParameter.put("client_secret", CLIENT_SECRET);
        expectedIdamApiParameter.put("username", SOME_SYSTEM_USER);
        expectedIdamApiParameter.put("password", SYSTEM_USER_PASS);
        expectedIdamApiParameter.put("scope", SCOPE);

        verify(idamApi).token(eq(expectedIdamApiParameter));
    }

    @Test
    void getUserDetails_from_am_and_idam() {

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedIdamRoles = Arrays.asList("role-1", "role-2");
        List<String> expectedAmRoles = Arrays.asList("role-3", "role-4");
        String expectedEmailAddress = "john.doe@example.com";
        String expectedForename = "John";
        String expectedSurname = "Doe";
        String expectedName = expectedForename + " " + expectedSurname;

        UserInfo expecteduUerInfo = new UserInfo(
            expectedEmailAddress,
            expectedId,
            expectedIdamRoles,
            expectedName,
            expectedForename,
            expectedSurname
        );
        when(idamApi.userInfo(anyString())).thenReturn(expecteduUerInfo);
        when(roleAssignmentService.getAmRolesFromUser(expectedId, expectedAccessToken))
            .thenReturn(expectedAmRoles);
        UserInfo actualUserInfo = idamService.getUserInfo(expectedAccessToken);
        verify(idamApi).userInfo(expectedAccessToken);
        List<String> expectedRoles = Stream.concat(expectedAmRoles.stream(), expectedIdamRoles.stream()).toList();

        assertEquals(expectedId, actualUserInfo.getUid());
        assertEquals(expectedRoles, actualUserInfo.getRoles());
        assertEquals(expectedEmailAddress, actualUserInfo.getEmail());
        assertEquals(expectedForename, actualUserInfo.getGivenName());
        assertEquals(expectedSurname, actualUserInfo.getFamilyName());
    }


    @Test
    void getUserDetails_from_idam() {

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedIdamRoles = Arrays.asList("role-1", "role-2");
        String expectedEmailAddress = "john.doe@example.com";
        String expectedForename = "John";
        String expectedSurname = "Doe";
        String expectedName = expectedForename + " " + expectedSurname;

        UserInfo expecteduUerInfo = new UserInfo(
            expectedEmailAddress,
            expectedId,
            expectedIdamRoles,
            expectedName,
            expectedForename,
            expectedSurname
        );
        when(idamApi.userInfo(anyString())).thenReturn(expecteduUerInfo);
        when(roleAssignmentService.getAmRolesFromUser(expectedId, expectedAccessToken))
            .thenReturn(Collections.emptyList());
        UserInfo actualUserInfo = idamService.getUserInfo(expectedAccessToken);
        verify(idamApi).userInfo(expectedAccessToken);

        assertEquals(expectedId, actualUserInfo.getUid());
        assertEquals(expectedIdamRoles, actualUserInfo.getRoles());
        assertEquals(expectedEmailAddress, actualUserInfo.getEmail());
        assertEquals(expectedForename, actualUserInfo.getGivenName());
        assertEquals(expectedSurname, actualUserInfo.getFamilyName());
    }

    @ParameterizedTest
    @CsvSource({"empty", "null"})
    void getUserDetails_from_am(String expectedIdamRoles) {

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedAmRoles = Arrays.asList("role-3", "role-4");
        String expectedEmailAddress = "john.doe@example.com";
        String expectedForename = "John";
        String expectedSurname = "Doe";
        String expectedName = expectedForename + " " + expectedSurname;

        UserInfo expecteduUerInfo = new UserInfo(
            expectedEmailAddress,
            expectedId,
            expectedIdamRoles.equals("null") ? null : Collections.emptyList(),
            expectedName,
            expectedForename,
            expectedSurname
        );
        when(idamApi.userInfo(anyString())).thenReturn(expecteduUerInfo);
        when(roleAssignmentService.getAmRolesFromUser(expectedId, expectedAccessToken))
            .thenReturn(expectedAmRoles);
        UserInfo actualUserInfo = idamService.getUserInfo(expectedAccessToken);
        verify(idamApi).userInfo(expectedAccessToken);

        assertEquals(expectedId, actualUserInfo.getUid());
        assertEquals(expectedAmRoles, actualUserInfo.getRoles());
        assertEquals(expectedEmailAddress, actualUserInfo.getEmail());
        assertEquals(expectedForename, actualUserInfo.getGivenName());
        assertEquals(expectedSurname, actualUserInfo.getFamilyName());
    }

    @Test
    void getUserDetails_from_am_idam_roles_null() {

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedAmRoles = Arrays.asList("role-3", "role-4");
        String expectedEmailAddress = "john.doe@example.com";
        String expectedForename = "John";
        String expectedSurname = "Doe";
        String expectedName = expectedForename + " " + expectedSurname;

        UserInfo expecteduUerInfo = new UserInfo(
            expectedEmailAddress,
            expectedId,
            null,
            expectedName,
            expectedForename,
            expectedSurname
        );
        when(idamApi.userInfo(anyString())).thenReturn(expecteduUerInfo);
        when(roleAssignmentService.getAmRolesFromUser(expectedId, expectedAccessToken))
            .thenReturn(expectedAmRoles);
        UserInfo actualUserInfo = idamService.getUserInfo(expectedAccessToken);
        verify(idamApi).userInfo(expectedAccessToken);

        assertEquals(expectedId, actualUserInfo.getUid());
        assertEquals(expectedAmRoles, actualUserInfo.getRoles());
        assertEquals(expectedEmailAddress, actualUserInfo.getEmail());
        assertEquals(expectedForename, actualUserInfo.getGivenName());
        assertEquals(expectedSurname, actualUserInfo.getFamilyName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"caseworker-ia-caseofficer", "caseworker-ia-iacjudge", "caseworker-ia-admofficer"})
    void getUserDetails_logs_exception_when_role_assignment_service_fails_for_onboarded_roles(String role) {
        Logger responseLogger = (Logger) LoggerFactory.getLogger(IdamService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        responseLogger.addAppender(listAppender);

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        String expectedEmailAddress = "john.doe@example.com";
        String expectedForename = "John";
        String expectedSurname = "Doe";
        String expectedName = expectedForename + " " + expectedSurname;

        when(roleAssignmentService.getAmRolesFromUser(expectedId, expectedAccessToken))
            .thenThrow(new NullPointerException("Role assignment service failed"));

        List<String> expectedIdamRoles = Arrays.asList(role, "role-2");
        UserInfo expecteduUerInfo = new UserInfo(
            expectedEmailAddress,
            expectedId,
            expectedIdamRoles,
            expectedName,
            expectedForename,
            expectedSurname
        );
        when(idamApi.userInfo(anyString())).thenReturn(expecteduUerInfo);
        idamService.getUserInfo(expectedAccessToken);
        List<ILoggingEvent> logEvents = listAppender.list;
        assertEquals(1, logEvents.size());
        assertEquals("Error fetching AM roles for user: 1234", logEvents.get(0).getFormattedMessage());

        verify(idamApi).userInfo(expectedAccessToken);
    }

    @Test
    void getUserDetails_does_not_log_exception_when_role_assignment_service_fails_for_non_onboarded_roles() {
        Logger responseLogger = (Logger) LoggerFactory.getLogger(IdamService.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        responseLogger.addAppender(listAppender);

        String expectedAccessToken = "ABCDEFG";
        String expectedId = "1234";
        List<String> expectedIdamRoles = Arrays.asList("role-1", "role-2");
        String expectedEmailAddress = "john.doe@example.com";
        String expectedForename = "John";
        String expectedSurname = "Doe";
        String expectedName = expectedForename + " " + expectedSurname;

        UserInfo expecteduUerInfo = new UserInfo(
            expectedEmailAddress,
            expectedId,
            expectedIdamRoles,
            expectedName,
            expectedForename,
            expectedSurname
        );
        when(idamApi.userInfo(anyString())).thenReturn(expecteduUerInfo);
        when(roleAssignmentService.getAmRolesFromUser(expectedId, expectedAccessToken))
            .thenThrow(new NullPointerException("Role assignment service failed"));
        idamService.getUserInfo(expectedAccessToken);
        List<ILoggingEvent> logEvents = listAppender.list;
        assertTrue(logEvents.isEmpty());

        verify(idamApi).userInfo(expectedAccessToken);
    }
}
