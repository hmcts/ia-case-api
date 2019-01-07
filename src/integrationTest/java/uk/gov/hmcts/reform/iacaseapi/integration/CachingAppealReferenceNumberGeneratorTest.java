package uk.gov.hmcts.reform.iacaseapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType.RP;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDate;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CachingAppealReferenceNumberGenerator;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.serialization.Serializer;
import uk.gov.hmcts.reform.iacaseapi.integration.stubs.CcdMock;
import uk.gov.hmcts.reform.iacaseapi.integration.util.IdamStubbedSpringBootIntegrationTest;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CachingAppealReferenceNumberGeneratorTest extends IdamStubbedSpringBootIntegrationTest {

    @Autowired
    private CachingAppealReferenceNumberGenerator appealReferenceNumberGenerator;

    @Autowired
    private Serializer<List> ccdCaseListSerializer;

    @Autowired
    private DateProvider dateProvider;

    private CcdMock givenCcd;

    @Before
    public void createCcdMock() {
        givenCcd = new CcdMock(ccdCaseListSerializer);
    }

    @Before
    public void setUpDate() {
        when(dateProvider.now()).thenReturn(
                LocalDate.now()
                        .withYear(2018));
    }

    @Test
    public void returns_the_same_appeal_reference_number_for_the_same_case_id() throws JsonProcessingException {

        givenCcd.doesntHaveAnyExistingAppealCases();

        String maybeFirstAppealReferenceNumber =
                appealReferenceNumberGenerator.getNextAppealReferenceNumberFor(
                        1,
                        RP.toString()).get();

        String maybeSecondAppealReferenceNumber =
                appealReferenceNumberGenerator.getNextAppealReferenceNumberFor(
                        1,
                        RP.toString()).get();

        assertThat(maybeSecondAppealReferenceNumber  )
                .isEqualTo(maybeFirstAppealReferenceNumber);
    }

    @Test
    public void returns_different_appeal_reference_number_for_a_subsequent_case_id() throws JsonProcessingException {

        givenCcd.doesntHaveAnyExistingAppealCases();

        String maybeFirstAppealReferenceNumber =
                appealReferenceNumberGenerator.getNextAppealReferenceNumberFor(
                        1,
                        RP.toString()).get();

        String maybeSecondAppealReferenceNumber =
                appealReferenceNumberGenerator.getNextAppealReferenceNumberFor(
                        2,
                        RP.toString()).get();

        assertThat(maybeFirstAppealReferenceNumber)
                .isEqualTo("RP/50001/2018");

        assertThat(maybeSecondAppealReferenceNumber)
                .isEqualTo("RP/50002/2018");
    }
}
