package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.queryManagement;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Data
@Builder
public class LatestQuery {

    private String queryId;
    private YesOrNo isHearingRelated;
}