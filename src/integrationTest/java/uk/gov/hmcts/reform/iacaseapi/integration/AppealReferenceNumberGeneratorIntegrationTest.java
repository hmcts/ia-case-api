package uk.gov.hmcts.reform.iacaseapi.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static uk.gov.hmcts.reform.iacaseapi.integration.stubs.CcdMock.paginationMetadataUrl;
import static uk.gov.hmcts.reform.iacaseapi.integration.util.TestAsylumCaseBuilder.anAsylumCase;
import static uk.gov.hmcts.reform.iacaseapi.integration.util.TestFixtures.someListOfCasesIncludingButPriorTo;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberGenerator;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization.Serializer;
import uk.gov.hmcts.reform.iacaseapi.integration.stubs.CcdMock;
import uk.gov.hmcts.reform.iacaseapi.integration.util.IdamStubbedSpringBootIntegrationTest;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class AppealReferenceNumberGeneratorIntegrationTest extends IdamStubbedSpringBootIntegrationTest {

    @Autowired
    private AppealReferenceNumberGenerator appealReferenceNumberGenerator;

    @Autowired
    private Serializer<List> ccdCaseListSerializer;

    private CcdMock givenCcd;

    @Before
    public void createCcdMock() {
        givenCcd = new CcdMock(ccdCaseListSerializer);
    }

    @Test
    public void generates_revocation_of_protection_appeal_reference_number_when_there_are_no_previous_appeals_in_Ccd() throws JsonProcessingException {

        givenCcd.doesntHaveAnyExistingAppealCases();

        Optional<String> appealReferenceNumber =
            appealReferenceNumberGenerator
                .getNextAppealReferenceNumberFor(AsylumAppealType.RP.toString());

        assertThat(appealReferenceNumber.get(), is("RP/50001/2018"));
    }

    @Test
    public void generates_protection_appeal_reference_number_when_there_are_no_previous_appeals_in_ccd() throws JsonProcessingException {

        givenCcd.doesntHaveAnyExistingAppealCases();

        Optional<String> appealReferenceNumber =
            appealReferenceNumberGenerator
                .getNextAppealReferenceNumberFor(AsylumAppealType.PA.toString());

        assertThat(appealReferenceNumber.get(), is("PA/50001/2018"));

    }

    @Test
    public void generates_protection_appeal_reference_number_from_ccd_case_list() throws JsonProcessingException {

        givenCcd.returns(
            someListOfCasesIncludingButPriorTo(
                anAsylumCase().withAppealReference("PA/50019/2018")));

        Optional<String> appealReferenceNumber =
            appealReferenceNumberGenerator
                .getNextAppealReferenceNumberFor(AsylumAppealType.PA.toString());

        assertThat(appealReferenceNumber.get(), is("PA/50020/2018"));
    }

    @Test
    public void generates_revocation_of_protection_appeal_reference_number_from_ccd_case_list() throws JsonProcessingException {

        givenCcd.returns(
            someListOfCasesIncludingButPriorTo(
                anAsylumCase().withAppealReference("RP/50019/2018")));

        Optional<String> appealReferenceNumber =
            appealReferenceNumberGenerator
                .getNextAppealReferenceNumberFor(AsylumAppealType.RP.toString());

        assertThat(appealReferenceNumber.get(), is("RP/50020/2018"));
    }

    @Test
    public void generates_appeal_reference_numbers_for_each_type_from_ccd_case_list() throws JsonProcessingException {

        givenCcd.returns(
            someListOfCasesIncludingButPriorTo(
                anAsylumCase().withAppealReference("RP/50019/2018")));

        Optional<String> protectionAppealReferenceNumber =
            appealReferenceNumberGenerator
                .getNextAppealReferenceNumberFor(AsylumAppealType.PA.toString());

        assertThat(protectionAppealReferenceNumber.get(), is("PA/50020/2018"));

        Optional<String> revocationAppealReferenceNumber =
            appealReferenceNumberGenerator
                .getNextAppealReferenceNumberFor(AsylumAppealType.RP.toString());

        assertThat(revocationAppealReferenceNumber.get(), is("RP/50020/2018"));
    }

    @Test
    public void returns_error_when_unable_to_initialize_reference_numbers() {

        givenCcd.hadAnInternalError();

        Optional<String> appealReferenceNumber =
            appealReferenceNumberGenerator
                .getNextAppealReferenceNumberFor(AsylumAppealType.RP.toString());

        assertFalse(appealReferenceNumber.isPresent());
    }

    @Test
    public void client_retries_3_times_when_connection_to_ccd_fails_next_call_succeeds() throws JsonProcessingException {

        givenCcd.hadAnInternalError();

        appealReferenceNumberGenerator
            .getNextAppealReferenceNumberFor(AsylumAppealType.PA.toString());

        verify(3, getRequestedFor(urlEqualTo(paginationMetadataUrl)));

        givenCcd.doesntHaveAnyExistingAppealCases();

        Optional<String> appealReferenceNumber =
            appealReferenceNumberGenerator
                .getNextAppealReferenceNumberFor(AsylumAppealType.PA.toString());

        assertThat(appealReferenceNumber.get(), is("PA/50001/2018"));
    }
}
