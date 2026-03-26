package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LatestQuery {

    private String queryId;
    private YesOrNo isHearingRelated;
}