package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPostSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;

public interface CcdEventPostSubmitHandler<T extends CaseData> {

    boolean canHandle(
        Stage stage,
        CcdEvent<T> ccdEvent
    );

    CcdEventPostSubmitResponse handle(
        Stage stage,
        CcdEvent<T> ccdEvent
    );
}
