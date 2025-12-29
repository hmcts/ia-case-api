package uk.gov.hmcts.reform.iacaseapi.domain.service;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;

public interface DataFixer {
    void fix(BailCase bailCase);

}
