package uk.gov.hmcts.reform.iacaseapi.shared.domain.entities;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.IdValue;

public class CaseNotes {

    private Optional<List<IdValue<CaseNote>>> caseNotes = Optional.empty();

    public Optional<List<IdValue<CaseNote>>> getCaseNotes() {
        return caseNotes;
    }

    public void setCaseNotes(List<IdValue<CaseNote>> caseNotes) {
        this.caseNotes = Optional.ofNullable(caseNotes);
    }
}
