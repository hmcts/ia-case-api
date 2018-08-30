package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventPreSubmitResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;

public interface CcdEventPreSubmitHandler<T extends CaseData> {

    boolean canHandle(
        Stage stage,
        CcdEvent<T> ccdEvent
    );

    CcdEventPreSubmitResponse<T> handle(
        Stage stage,
        CcdEvent<T> ccdEvent
    );
}
