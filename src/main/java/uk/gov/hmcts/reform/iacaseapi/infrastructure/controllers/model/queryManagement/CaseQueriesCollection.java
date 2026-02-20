package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model.queryManagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CaseQueriesCollection {

    private String partyName;
    private String roleOnCase;
    private List<CaseMessage> caseMessages;
}
