package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;

public interface CcdEventPreSubmitHandler<T extends CaseData> {

    boolean canHandle(
        Stage stage,
        CcdEvent<T> ccdEvent
    );

    default DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATE;
    }

    CcdEventPreSubmitResponse<T> handle(
        Stage stage,
        CcdEvent<T> ccdEvent
    );
}
