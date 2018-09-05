package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.List;
import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.IdValue;

public class Issues {

    private Optional<List<IdValue<String>>> issues = Optional.empty();

    public Optional<List<IdValue<String>>> getIssues() {
        return issues;
    }

    public void setIssues(List<IdValue<String>> issues) {
        this.issues = Optional.ofNullable(issues);
    }
}
