package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.allocatecase;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ALLOCATE_THE_CASE_TO;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;

@Component
public class AllocateTheCaseService {

    public boolean isAllocateToCaseWorkerOption(AsylumCase asylumCase) {
        String allocateTheCaseTo = asylumCase.read(ALLOCATE_THE_CASE_TO, String.class)
            .orElse(StringUtils.EMPTY);
        return "caseworker".equals(allocateTheCaseTo);
    }
}