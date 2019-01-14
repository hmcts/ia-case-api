package uk.gov.hmcts.reform.iacaseapi.domain.service;

import java.util.Map;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.AppealReferenceNumber;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AsylumAppealType;

public interface AppealReferenceNumberInitializer {

    Map<AsylumAppealType, AppealReferenceNumber> initialize();
}
