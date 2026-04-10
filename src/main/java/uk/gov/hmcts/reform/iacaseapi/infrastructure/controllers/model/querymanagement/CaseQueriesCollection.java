package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class CaseQueriesCollection {

    private String partyName;
    private String roleOnCase;
    private  List<IdValue<CaseMessage>> caseMessages;
}
