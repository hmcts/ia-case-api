package uk.gov.hmcts.reform.iacaseapi.component;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SHARE_A_CASE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.DECISION;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithReferenceDataStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithRoleAssignmentStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithUserDetailsStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.PreSubmitCallbackResponseForTest;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;

@Slf4j
class ShareACaseCcdIntegrationTest extends SpringBootIntegrationTest implements WithServiceAuthStub,
    WithUserDetailsStub, WithReferenceDataStub, WithRoleAssignmentStub {

    private static final String ACTIVE_USER_ID = "6c4fd62d-9d3c-4d11-962c-57080df16871";

    private static final String CCD_ACCESS_API_PATH =
        "/caseworkers/{idamIdOfUserWhoGrantsAccess}/jurisdictions/{jurisdiction}/case-types/{caseType}/cases/{caseId}/users";

    private final Value value1 = new Value("another-user-id", "email@somewhere.com");
    private final Value value2 = new Value(ACTIVE_USER_ID, "email@somewhere.com");

    private final List<Value> values = Lists.newArrayList(value1, value2);

    private DynamicList dynamicList;

    @org.springframework.beans.factory.annotation.Value("classpath:prd-org-users-response.json")
    private Resource resourceFile;

    @org.springframework.beans.factory.annotation.Value("${prof.ref.data.path.org.users}")
    private String refDataPath;

    private String prdResponseJson;

    @BeforeEach
    void setupReferenceDataStub() throws IOException {
        prdResponseJson =
            new String(Files.readAllBytes(Paths.get(resourceFile.getURI())));

        assertThat(prdResponseJson).isNotBlank();
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-legalrep-solicitor"})
    void should_return_success_when_user_is_valid_and_201_returned_from_ccd() {
        addServiceAuthStub(server);
        addLegalRepUserDetailsStub(server);
        addReferenceDataPrdResponseStub(server, refDataPath, prdResponseJson);
        addRoleAssignmentQueryStub(server);
        dynamicList = new DynamicList(value2, values);

        //Userdetails stub will always return uid 1
        String idamUserId = "1";
        long caseId = 9999L;
        URI uri = buildUri(idamUserId, String.valueOf(caseId));
        addReferenceCreatedStub(server, uri.getPath());

        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
            .event(SHARE_A_CASE)
            .caseDetails(someCaseDetailsWith()
                .id(caseId)
                .state(DECISION)
                .caseData(anAsylumCase()
                    .with(ORG_LIST_OF_USERS, dynamicList)
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPEAL_TYPE, AppealType.PA)
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        assertThat(response).isNotNull();
        assertThat(response.getErrors()).isEmpty();
        assertEquals(Optional.empty(), response.getAsylumCase().read(ORG_LIST_OF_USERS));

    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia", "caseworker-ia-legalrep-solicitor"})
    void should_return_failure_when_user_is_invalid() {
        addServiceAuthStub(server);
        addLegalRepUserDetailsStub(server);
        addReferenceDataPrdResponseStub(server, refDataPath, prdResponseJson);
        addRoleAssignmentQueryStub(server);

        // invalid user is chosen in dropdown
        dynamicList = new DynamicList(value1, values);

        //Userdetails stub will always return uid 1
        String idamUserId = "1";
        long caseId = 9999L;
        URI uri = buildUri(idamUserId, String.valueOf(caseId));
        addReferenceCreatedStub(server, uri.getPath());
        PreSubmitCallbackResponseForTest response = iaCaseApiClient.aboutToSubmit(callback()
            .event(SHARE_A_CASE)
            .caseDetails(someCaseDetailsWith()
                .id(caseId)
                .state(DECISION)
                .caseData(anAsylumCase()
                    .with(ORG_LIST_OF_USERS, dynamicList)
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPEAL_TYPE, AppealType.PA)
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        assertThat(response).isNotNull();
        assertThat(response.getErrors()).contains("You can share a case only with Active Users in your Organization.");
        assertEquals(Optional.empty(), response.getAsylumCase().read(ORG_LIST_OF_USERS));

    }

    private URI buildUri(String idamUserId,
                         String caseId) {
        return UriComponentsBuilder
            .fromPath(CCD_ACCESS_API_PATH)
            .build(idamUserId, "ia", "Asylum", caseId);
    }
}
