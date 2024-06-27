package uk.gov.hmcts.reform.iacaseapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SHARE_A_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_STARTED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.DECISION;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithReferenceDataStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithUserDetailsStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.OrganisationPolicy;

class ShareACaseRefDataIntegrationTest extends SpringBootIntegrationTest implements WithServiceAuthStub,
    WithUserDetailsStub, WithReferenceDataStub {

    @org.springframework.beans.factory.annotation.Value("classpath:prd-org-users-response.json")
    private Resource resourceFile;

    @org.springframework.beans.factory.annotation.Value("classpath:prd-org-users-no-org-id-response.json")
    private Resource resourceFileNoOrgId;

    @org.springframework.beans.factory.annotation.Value("${prof.ref.data.path.org.users}")
    private String refDataPath;

    private ProfessionalUsersResponse prdSuccessResponse;

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-legalrep-solicitor"})
    void should_get_users_from_professional_ref_data() throws Exception {

        String prdResponseJson =
            new String(Files.readAllBytes(Paths.get(resourceFile.getURI())));

        assertThat(prdResponseJson).isNotBlank();

        prdSuccessResponse = objectMapper.readValue(prdResponseJson,
            ProfessionalUsersResponse.class);

        addServiceAuthStub(server);
        addLegalRepUserDetailsStub(server);
        addReferenceDataPrdResponseStub(server, refDataPath, prdResponseJson);

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToStart(callback()
            .event(SHARE_A_CASE)
            .caseDetails(someCaseDetailsWith()
                .state(DECISION)
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPEAL_TYPE, AppealType.PA)
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        AsylumCase asylumCase = response.getAsylumCase();

        Optional<DynamicList> listOfUsers = asylumCase.read(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS);
        assertThat(listOfUsers.isPresent()).as("No colleagueId returned").isTrue();

        DynamicList dynamicList = listOfUsers.orElseThrow(() -> new IllegalStateException("no users returned"));

        List<Value> listItems = dynamicList.getListItems();
        assertThat(listItems.size()).isEqualTo(2);

        assertThat(listItems.get(0).getCode()).isEqualTo(prdSuccessResponse.getUsers().get(0).getUserIdentifier());
        assertThat(listItems.get(1).getCode()).isEqualTo(prdSuccessResponse.getUsers().get(1).getUserIdentifier());
        assertThat(listItems.get(0).getLabel()).isEqualTo(prdSuccessResponse.getUsers().get(0).getEmail());
        assertThat(listItems.get(1).getLabel()).isEqualTo(prdSuccessResponse.getUsers().get(1).getEmail());

    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-legalrep-solicitor"})
    void should_get_users_from_professional_ref_data_no_org_id() throws Exception {

        String prdResponseJsonNoOrgId =
            new String(Files.readAllBytes(Paths.get(resourceFileNoOrgId.getURI())));

        assertThat(prdResponseJsonNoOrgId).isNotBlank();

        prdSuccessResponse = objectMapper.readValue(prdResponseJsonNoOrgId,
            ProfessionalUsersResponse.class);

        addServiceAuthStub(server);
        addLegalRepUserDetailsStub(server);
        addReferenceDataPrdResponseStub(server, refDataPath, prdResponseJsonNoOrgId);

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToStart(callback()
            .event(SHARE_A_CASE)
            .caseDetails(someCaseDetailsWith()
                .state(DECISION)
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "no-org-id-appeal-reference-number")
                    .with(APPEAL_TYPE, AppealType.PA)
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        AsylumCase asylumCase = response.getAsylumCase();

        Optional<DynamicList> listOfUsers = asylumCase.read(AsylumCaseFieldDefinition.ORG_LIST_OF_USERS);
        assertThat(listOfUsers.isPresent()).as("No colleagueId returned").isTrue();

        DynamicList dynamicList = listOfUsers.orElseThrow(() -> new IllegalStateException("no users returned"));

        List<Value> listItems = dynamicList.getListItems();
        assertThat(listItems.size()).isEqualTo(2);

        assertThat(listItems.get(0).getCode()).isEqualTo(prdSuccessResponse.getUsers().get(0).getUserIdentifier());
        assertThat(listItems.get(1).getCode()).isEqualTo(prdSuccessResponse.getUsers().get(1).getUserIdentifier());
        assertThat(listItems.get(0).getLabel()).isEqualTo(prdSuccessResponse.getUsers().get(0).getEmail());
        assertThat(listItems.get(1).getLabel()).isEqualTo(prdSuccessResponse.getUsers().get(1).getEmail());

    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-legalrep-solicitor"})
    void should_get_organisation_identifier_from_professional_ref_data() throws Exception {

        String prdResponseJson =
            new String(Files.readAllBytes(Paths.get(resourceFile.getURI())));

        assertThat(prdResponseJson).isNotBlank();

        prdSuccessResponse = objectMapper.readValue(prdResponseJson,
            ProfessionalUsersResponse.class);

        addServiceAuthStub(server);
        addLegalRepUserDetailsStub(server);
        addReferenceDataPrdResponseStub(server, refDataPath, prdResponseJson);

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToStart(callback()
            .event(START_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(APPEAL_STARTED)
                .caseData(anAsylumCase()
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPEAL_TYPE, AppealType.PA)
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        AsylumCase asylumCase = response.getAsylumCase();

        Optional<OrganisationPolicy> organisationPolicy = asylumCase.read(AsylumCaseFieldDefinition.LOCAL_AUTHORITY_POLICY);
        assertThat(organisationPolicy.isPresent());

    }

    @BeforeEach
    @SneakyThrows
    @SuppressWarnings("java:S2925")
    void makeAPause() {
        /*
            We are not using Wiremock the way it's intended to be used. It should be used by
            starting a webserver at the beginning of all tests and taking it down at the end, but
            what we do is spinning up and down the server all the time and change its mappings
            all the time.
            The result is that its behaviour is somewhat flaky.

            The following pause is meant to allow Wiremock time to conclude some operations that
            we invoke.
         */
        Thread.sleep(1000);
    }
}
