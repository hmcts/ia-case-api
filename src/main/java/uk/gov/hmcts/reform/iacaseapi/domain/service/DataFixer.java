package uk.gov.hmcts.reform.iacaseapi.domain.service;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;

public interface DataFixer<T extends CaseData> {
    void fix(T caseData);
}
