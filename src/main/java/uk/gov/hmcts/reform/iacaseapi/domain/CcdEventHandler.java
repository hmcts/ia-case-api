package uk.gov.hmcts.reform.iacaseapi.domain;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CcdEventResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Stage;

public interface CcdEventHandler<T extends CaseData> {

    boolean canHandle(
        Stage stage,
        CcdEvent<T> ccdEvent
    );

    CcdEventResponse<T> handle(
        Stage stage,
        CcdEvent<T> ccdEvent
    );
}
