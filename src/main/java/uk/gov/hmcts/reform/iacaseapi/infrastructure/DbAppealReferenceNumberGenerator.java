package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberGenerator;

@Service
public class DbAppealReferenceNumberGenerator implements AppealReferenceNumberGenerator {

    public String generate(long caseId, AppealType appealType) {

        return appealType.name() + "/" + caseId;
    }

}
