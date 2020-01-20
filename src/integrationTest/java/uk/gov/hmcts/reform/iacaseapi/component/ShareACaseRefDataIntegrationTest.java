package uk.gov.hmcts.reform.iacaseapi.component;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.UserDetailsForTest.UserDetailsForTestBuilder.userWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SHARE_A_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.DECISION;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;

public class ShareACaseRefDataIntegrationTest extends SpringBootIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    private ProfessionalUsersResponse prdSuccessResponse;

    @org.springframework.beans.factory.annotation.Value("classpath:prd-org-users-response.json")
    private Resource resourceFile;

    @org.springframework.beans.factory.annotation.Value("${prof.ref.data.path.org.users}")
    private String refDataPath;

    @Before
    public void setupReferenceDataStub() throws IOException {

        String prdResponseJson =
            new String(Files.readAllBytes(Paths.get(resourceFile.getURI())));

        assertThat(prdResponseJson).isNotBlank();

        prdSuccessResponse = objectMapper.readValue(prdResponseJson, ProfessionalUsersResponse.class);

        stubFor(get(urlEqualTo(refDataPath))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(prdResponseJson)));
    }

    @Test
    public void should_get_users_from_professional_ref_data() {

        given.someLoggedIn(userWith()
            .roles(newHashSet("caseworker-ia", "caseworker-ia-legalrep-solicitor")));

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToStart(callback()
            .event(SHARE_A_CASE)
            .caseDetails(someCaseDetailsWith()
                .state(DECISION)
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        AsylumCase asylumCase = response.getAsylumCase();

        Optional<DynamicList> listOfUsers = asylumCase.read(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS);
        assertThat(listOfUsers.isPresent()).as("No colleagueId returned").isTrue();

        DynamicList dynamicList = listOfUsers.orElseThrow(() -> new IllegalStateException("no users returned"));

        List<Value> listItems = dynamicList.getListItems();
        assertThat(listItems.size()).isEqualTo(2);

        assertThat(listItems.get(0).getCode()).isEqualTo(prdSuccessResponse.getProfessionalUsers().get(0).getUserIdentifier());
        assertThat(listItems.get(1).getCode()).isEqualTo(prdSuccessResponse.getProfessionalUsers().get(1).getUserIdentifier());
        assertThat(listItems.get(0).getLabel()).isEqualTo(prdSuccessResponse.getProfessionalUsers().get(0).getEmail());
        assertThat(listItems.get(1).getLabel()).isEqualTo(prdSuccessResponse.getProfessionalUsers().get(1).getEmail());

    }

}
