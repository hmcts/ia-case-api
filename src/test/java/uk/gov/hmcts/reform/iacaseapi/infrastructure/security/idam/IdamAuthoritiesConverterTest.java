package uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.ACCESS_TOKEN;
import static uk.gov.hmcts.reform.iacaseapi.infrastructure.security.idam.IdamAuthoritiesConverter.TOKEN_NAME;

import com.google.common.collect.Lists;
import feign.FeignException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.IdamApi;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.idam.UserInfo;


@ExtendWith(MockitoExtension.class)
class IdamAuthoritiesConverterTest {

    @Mock
    private org.springframework.security.oauth2.jwt.Jwt jwt;

    @Mock
    private IdamApi idamApi;

    @Mock
    private UserInfo userInfo;

    private String tokenValue =
        "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJjY2QtaW1wb3J0QGZha2UuaG1jdHMubmV0IiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiZDg3ODI3ODQtMWU0NC00NjkyLTg0NzgtNTI5MzE0NTVhNGI5IiwiaXNzIjoiaHR0cDovL2ZyLWFtOjgwODAvb3BlbmFtL29hdXRoMi9obWN0cyIsInRva2VuTmFtZSI6ImFjY2Vzc190b2tlbiIsInRva2VuX3R5cGUiOiJCZWFyZXIiLCJhdXRoR3JhbnRJZCI6IjNjMWMzNjFkLTRlYzUtNGY0NS1iYzI0LTUxOGMzMDk0MzUxYiIsImF1ZCI6ImNjZF9nYXRld2F5IiwibmJmIjoxNTg0NTI2MzcyLCJncmFudF90eXBlIjoicGFzc3dvcmQiLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiXSwiYXV0aF90aW1lIjoxNTg0NTI2MzcyLCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTU4NDU1NTE3MiwiaWF0IjoxNTg0NTI2MzcyLCJleHBpcmVzX2luIjoyODgwMCwianRpIjoiNDhjNDMzYTQtZmRiNS00YTIwLWFmNGUtMmYyNjIyYjYzZmU5In0.WP8ATcHMmdtG2W443aqNz3ES6-Bqng0IKjTQfbndN1HrBLJWJtpC3qfzy2wD_CdiPU4uspdN5S91nhiT8Ub6DjstnDz3VPmR3Cbdk5QJBdAsQ0ah9w-duS8SA_dlzDIMt18bSDMUUdck6YxsoNyQFisI6cKNnfgB9ZTLhenVENtdmyrKVr96Ezp-jhhzmMVMxb1rW7KghSAH0ZCsWqlhrM--jPGRCweDiFe-ldi4EuhIxGbkPjyWwsdcgmYfIuFrSxqV0vrSI37DNZx_Sh5DVJpUgSrYKRzuMqe4rFN6WVyHIY_Qu52ER2yrNYtGbAQ5AyMabPTPj9VVxqpa5nYUAg";

    private IdamAuthoritiesConverter idamAuthoritiesConverter;

    @Test
    void should_return_correct_granted_authority_collection() {

        when(jwt.hasClaim(TOKEN_NAME)).thenReturn(true);
        when(jwt.getClaim(TOKEN_NAME)).thenReturn(ACCESS_TOKEN);
        when(jwt.getTokenValue()).thenReturn(tokenValue);

        when(userInfo.getRoles()).thenReturn(Lists.newArrayList("caseworker-ia", "caseworker-ia-caseofficer"));
        when(idamApi.userInfo("Bearer " + tokenValue)).thenReturn(userInfo);

        idamAuthoritiesConverter = new IdamAuthoritiesConverter(idamApi);

        List<GrantedAuthority> expectedGrantedAuthorities = Lists.newArrayList(
            new SimpleGrantedAuthority("caseworker-ia"),
            new SimpleGrantedAuthority("caseworker-ia-caseofficer")
        );

        Collection<GrantedAuthority> grantedAuthorities = idamAuthoritiesConverter.convert(jwt);

        verify(idamApi).userInfo("Bearer " + tokenValue);

        assertEquals(expectedGrantedAuthorities, grantedAuthorities);
    }

    @Test
    void should_return_empty_list_when_token_is_missing() {

        idamAuthoritiesConverter = new IdamAuthoritiesConverter(idamApi);

        assertEquals(Collections.emptyList(), idamAuthoritiesConverter.convert(jwt));
    }

    @Test
    void should_return_empty_list_when_user_info_does_not_contain_roles() {

        when(userInfo.getRoles()).thenReturn(Lists.newArrayList());
        when(idamApi.userInfo("Bearer " + tokenValue)).thenReturn(userInfo);

        idamAuthoritiesConverter = new IdamAuthoritiesConverter(idamApi);

        when(jwt.hasClaim(TOKEN_NAME)).thenReturn(true);
        when(jwt.getClaim(TOKEN_NAME)).thenReturn(ACCESS_TOKEN);
        when(jwt.getTokenValue()).thenReturn(tokenValue);

        assertEquals(Collections.emptyList(), idamAuthoritiesConverter.convert(jwt));
    }

    @Test
    void should_throw_exception_when_auth_service_unavailable() {

        when(idamApi.userInfo("Bearer " + tokenValue)).thenThrow(FeignException.class);

        when(jwt.hasClaim(TOKEN_NAME)).thenReturn(true);
        when(jwt.getClaim(TOKEN_NAME)).thenReturn(ACCESS_TOKEN);
        when(jwt.getTokenValue()).thenReturn(tokenValue);

        idamAuthoritiesConverter = new IdamAuthoritiesConverter(idamApi);

        IdentityManagerResponseException thrown = assertThrows(
            IdentityManagerResponseException.class,
            () -> idamAuthoritiesConverter.convert(jwt)
        );
        assertEquals("Could not get user details from IDAM", thrown.getMessage());
    }
}
