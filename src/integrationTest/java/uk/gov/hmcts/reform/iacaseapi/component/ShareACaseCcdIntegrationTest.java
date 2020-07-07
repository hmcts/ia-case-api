package uk.gov.hmcts.reform.iacaseapi.component;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder.userWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SHARE_A_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.DECISION;

import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ProfessionalUsersResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;

@Slf4j
public class ShareACaseCcdIntegrationTest extends SpringBootIntegrationTest {

    private static final String ACTIVE_USER_ID = "6c4fd62d-9d3c-4d11-962c-57080df16871";

    private static final String CCD_ACCESS_API_PATH =
        "/caseworkers/{idamIdOfUserWhoGrantsAccess}/jurisdictions/{jurisdiction}/case-types/{caseType}/cases/{caseId}/users";

    private Value value1 = new Value("another-user-id", "email@somewhere.com");
    private Value value2 = new Value(ACTIVE_USER_ID, "email@somewhere.com");

    private List<Value> values = Lists.newArrayList(value1, value2);

    private DynamicList dynamicList;

    @org.springframework.beans.factory.annotation.Value("classpath:prd-org-users-response.json")
    private Resource resourceFile;

    @org.springframework.beans.factory.annotation.Value("${prof.ref.data.path.org.users}")
    private String refDataPath;

    protected ProfessionalUsersResponse prdSuccessResponse;

    @Before
    public void setupReferenceDataStub() throws IOException {

        String prdResponseJson =
            new String(Files.readAllBytes(Paths.get(resourceFile.getURI())));

        assertThat(prdResponseJson).isNotBlank();

        prdSuccessResponse = objectMapper.readValue(prdResponseJson,
            ProfessionalUsersResponse.class);

        stubFor(get(urlEqualTo(refDataPath))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(prdResponseJson)));
    }

    @Test
    public void should_return_success_when_user_is_valid_and_201_returned_from_ccd() {

        dynamicList = new DynamicList(value2, values);

        String idamUserId = "idam-user-id";
        long caseId = 9999L;
        given.someLoggedIn(userWith()
            .id(idamUserId)
            .roles(newHashSet("caseworker-ia", "caseworker-ia-legalrep-solicitor"))
        );

        URI uri = buildUri(idamUserId, String.valueOf(caseId));

        prepareCcdDataStoreResponse(uri.getPath(), HttpStatus.CREATED.value());

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
            .event(SHARE_A_CASE)
            .caseDetails(someCaseDetailsWith()
                .id(caseId)
                .state(DECISION)
                .caseData(anAsylumCase()
                    .with(ORG_LIST_OF_USERS, dynamicList)
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();
        assertEquals(Optional.empty(), response.getAsylumCase().read(ORG_LIST_OF_USERS));

        verify(1, RequestPatternBuilder.allRequests()
            .withUrl("/ccd-data-store" + uri.getPath()));

    }

    @Test
    public void should_return_failure_when_user_is_invalid() {

        // invalid user is chosen in dropdown
        dynamicList = new DynamicList(value1, values);

        String idamUserId = "idam-user-id";
        long caseId = 9999L;
        given.someLoggedIn(userWith()
            .id(idamUserId)
            .roles(newHashSet("caseworker-ia", "caseworker-ia-legalrep-solicitor"))
        );

        URI uri = buildUri(idamUserId, String.valueOf(caseId));

        prepareCcdDataStoreResponse(uri.getPath(), HttpStatus.CREATED.value());

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
            .event(SHARE_A_CASE)
            .caseDetails(someCaseDetailsWith()
                .id(caseId)
                .state(DECISION)
                .caseData(anAsylumCase()
                    .with(ORG_LIST_OF_USERS, dynamicList)
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        assertThat(response).isNotNull();
        assertThat(response.getErrors()).contains("You can share a case only with Active Users in your Organization.");
        assertEquals(Optional.empty(), response.getAsylumCase().read(ORG_LIST_OF_USERS));

        verify(0, RequestPatternBuilder.allRequests()
            .withUrl("/ccd-data-store" + uri.getPath()));

    }

    public void prepareCcdDataStoreResponse(String path, int expectedStatus) {

        String stubPrefix = "/ccd-data-store";
        log.info("Ccd data store uri being called in test: {}", path);

        stubFor(post(urlEqualTo(stubPrefix + path))
            .willReturn(aResponse()
                .withStatus(expectedStatus)
                .withHeader(HttpHeaders.CONTENT_TYPE,
                    MediaType.APPLICATION_JSON.toString()))
        );

    }

    private URI buildUri(String idamUserId,
                         String caseId) {
        return UriComponentsBuilder
            .fromPath(CCD_ACCESS_API_PATH)
            .build(idamUserId, "ia", "Asylum", caseId);
    }

}
