package uk.gov.hmcts.reform.iacaseapi.domain.service;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;

public interface DataFixer {

    void fix(AsylumCase asylumCase);
}
