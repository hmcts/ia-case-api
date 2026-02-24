package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.querymanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

import java.util.List;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaseQueriesCollection {

    private String partyName;
    private String roleOnCase;
    private  List<IdValue<CaseMessage>> caseMessages;
}
