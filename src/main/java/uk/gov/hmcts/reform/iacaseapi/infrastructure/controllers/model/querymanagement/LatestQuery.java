package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class LatestQuery {

    private String queryId;
    private YesOrNo isHearingRelated;
}