package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Map;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealReferenceNumber;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumAppealType;

public interface AppealReferenceNumberInitializer {

    Map<AsylumAppealType, AppealReferenceNumber> initialize();
}
