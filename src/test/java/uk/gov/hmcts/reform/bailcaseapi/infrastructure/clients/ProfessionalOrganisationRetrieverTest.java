package uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ref.OrganisationEntityResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"unchecked", "rawtypes"})
public class ProfessionalOrganisationRetrieverTest {

    private ProfessionalOrganisationRetriever professionalOrganisationRetriever;

    private final String refdataUrl = "http:/some-url";
    private final String refdataPath = "/some-path";

    @Mock private AuthTokenGenerator serviceAuthTokenGenerator;
    @Mock private UserDetailsProvider userDetailsProvider;
    @Mock private RestTemplate restTemplate;
    @Mock private ResponseEntity responseEntity;

    @Before
    public void setup() {

        professionalOrganisationRetriever = new ProfessionalOrganisationRetriever(restTemplate,
            serviceAuthTokenGenerator,
            userDetailsProvider,
            refdataUrl,
            refdataPath);
    }

    @Test
    public void should_successfully_get_prof_orgaisation_response() {

        final String expectedServiceToken = "ABCDEFG";
        final String expectedAccessToken = "HIJKLMN";
        final OrganisationEntityResponse organisationResponse =
            mock(OrganisationEntityResponse.class);
        final UserDetails userDetails = mock(UserDetails.class);

        when(serviceAuthTokenGenerator.generate()).thenReturn(expectedServiceToken);
        when(userDetailsProvider.getUserDetails()).thenReturn(userDetails);
        when(userDetails.getAccessToken()).thenReturn(expectedAccessToken);

        doReturn(responseEntity)
            .when(restTemplate)
            .exchange(
                eq(refdataUrl + refdataPath),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
            );

        when(responseEntity.getBody()).thenReturn(organisationResponse);

        OrganisationEntityResponse response = professionalOrganisationRetriever.retrieve();

        assertThat(response).isNotNull().isInstanceOf(OrganisationEntityResponse.class);

        verify(restTemplate).exchange(anyString(),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            any(ParameterizedTypeReference.class));

        verifyNoMoreInteractions(organisationResponse);
    }
}
