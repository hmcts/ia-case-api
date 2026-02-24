package uk.gov.hmcts.reform.iacaseapi.component;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.AsylumCaseForTest.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CallbackForTest.CallbackForTestBuilder.callback;
import static uk.gov.hmcts.reform.iacaseapi.component.testutils.fixtures.CaseDetailsForTest.CaseDetailsForTestBuilder.someCaseDetailsWith;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_NOTIFICATION_TURNED_OFF;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State.APPEAL_STARTED;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.security.test.context.support.WithMockUser;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.SpringBootIntegrationTest;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithDocumentApiStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithNotificationsApiStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithReferenceDataStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithRoleAssignmentStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithServiceAuthStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithTimedEventServiceStub;
import uk.gov.hmcts.reform.iacaseapi.component.testutils.WithUserDetailsStub;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

class DocumentApiDelgationTest extends SpringBootIntegrationTest implements WithUserDetailsStub,
    WithRoleAssignmentStub, WithServiceAuthStub, WithDocumentApiStub, WithReferenceDataStub,
    WithTimedEventServiceStub, WithNotificationsApiStub {

    @Value("classpath:prd-org-users-response.json")
    private Resource resourceFile;

    @org.springframework.beans.factory.annotation.Value("${prof.ref.data.path.org.users}")
    private String refDataPath;

    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    void doesnt_delegate_to_document_api() throws IOException {
        String prdResponseJson =
            new String(Files.readAllBytes(Paths.get(resourceFile.getURI())));

        assertThat(prdResponseJson).isNotBlank();

        addServiceAuthStub(server);
        addLegalRepUserDetailsStub(server);
        addReferenceDataPrdResponseStub(server, refDataPath, prdResponseJson);
        addRoleAssignmentActorStub(server);
        addDocumentApiTransformerStub(server);

        iaCaseApiClient.aboutToSubmit(callback()
            .event(SUBMIT_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(APPEAL_STARTED)
                .caseData(anAsylumCase()
                    .with(IS_NOTIFICATION_TURNED_OFF, YesOrNo.YES)
                    .with(HOME_OFFICE_DECISION_DATE, "2022-01-01")
                    .with(HOME_OFFICE_REFERENCE_NUMBER, "PA/50001/2022")
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPEAL_TYPE, AppealType.PA)
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        server.verify(0, postRequestedFor(urlEqualTo("/ia-case-documents-api/asylum/ccdAboutToSubmit")));
    }

    @Test
    @WithMockUser(authorities = {"caseworker-ia-legalrep-solicitor"})
    void does_delegate_to_document_api_when_notification_not_turned_off() throws IOException {
        String prdResponseJson =
            new String(Files.readAllBytes(Paths.get(resourceFile.getURI())));

        assertThat(prdResponseJson).isNotBlank();

        addServiceAuthStub(server);
        addLegalRepUserDetailsStub(server);
        addReferenceDataPrdResponseStub(server, refDataPath, prdResponseJson);
        addRoleAssignmentActorStub(server);
        addRoleAssignmentQueryStub(server);
        addDocumentApiTransformerStub(server);
        addTimedEventServiceStub(server);
        addNotificationsApiTransformerStub(server);

        iaCaseApiClient.aboutToSubmit(callback()
            .event(SUBMIT_APPEAL)
            .caseDetails(someCaseDetailsWith()
                .state(APPEAL_STARTED)
                .caseData(anAsylumCase()
                    .with(IS_NOTIFICATION_TURNED_OFF, YesOrNo.NO)
                    .with(HOME_OFFICE_DECISION_DATE, "2022-01-01")
                    .with(HOME_OFFICE_REFERENCE_NUMBER, "PA/50001/2022")
                    .with(APPEAL_REFERENCE_NUMBER, "some-appeal-reference-number")
                    .with(APPEAL_TYPE, AppealType.PA)
                    .with(APPELLANT_GIVEN_NAMES, "some-given-name")
                    .with(APPELLANT_FAMILY_NAME, "some-family-name"))));

        server.verify(1, postRequestedFor(urlEqualTo("/ia-case-documents-api/asylum/ccdAboutToSubmit")));
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
