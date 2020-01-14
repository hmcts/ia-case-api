package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class DbAppealReferenceNumberGeneratorTest {


    private final long caseId = 12345;

    @Test
    public void should_generate_new_appeal_reference_number_when_appeal_type_pa() {

        DbAppealReferenceNumberGenerator dbAppealReferenceNumberGenerator = new DbAppealReferenceNumberGenerator();

        for (AppealType type : AppealType.values()) {
            String appealReferenceNumber = dbAppealReferenceNumberGenerator.generate(caseId, type);

            assertEquals(type.name() + "/" + caseId, appealReferenceNumber);
        }

    }
}
