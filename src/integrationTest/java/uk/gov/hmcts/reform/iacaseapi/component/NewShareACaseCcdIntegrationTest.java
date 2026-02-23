package uk.gov.hmcts.reform.iacaseapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_STARTED;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.*;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PostSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;

@Slf4j
class NewShareACaseCcdIntegrationTest extends SpringBootIntegrationTest implements WithServiceAuthStub,
    WithCcdStub, WithAcaAssignmentsStub, WithUserDetailsStub, WithReferenceDataStub {

    @org.springframework.beans.factory.annotation.Value("classpath:prd-org-users-response.json")
    private Resource resourceFile;

    @org.springframework.beans.factory.annotation.Value("${prof.ref.data.path.org.users}")
    private String refDataPathUsers;

    @org.springframework.beans.factory.annotation.Value("${prof.ref.data.path.org.organisation}")
    private String refDataPathOrganisation;

    private String prdResponseJson;

    @BeforeEach
    void setupReferenceDataStub() throws IOException {

        prdResponseJson =
            new String(Files.readAllBytes(Paths.get(resourceFile.getURI())));

        assertThat(prdResponseJson).isNotBlank();
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-legalrep-solicitor"})
    void should_return_success_when_org_creator_access_revoked_and_case_assignment_set() {

        addServiceAuthStub(server);
        addLegalRepUserDetailsStub(server);
        addReferenceDataPrdResponseStub(server, refDataPathUsers, prdResponseJson);
        addReferenceDataPrdOrganisationResponseStub(server, refDataPathOrganisation, prdResponseJson);
        addCcdAssignmentsStub(server);
        addAcaAssignmentsStub(server);

        long caseId = 9999L;

        PostSubmitCallbackResponseForTest response = iaCaseApiClient.ccdSubmitted(callback()
            .event(START_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .id(caseId)
                .state(APPEAL_STARTED)
                .caseData(anAsylumCase()
                    .with(APPEAL_TYPE, AppealType.EA)
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        assertThat(response).isNotNull();
    }
}
