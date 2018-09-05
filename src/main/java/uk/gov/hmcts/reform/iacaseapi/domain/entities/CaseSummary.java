package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;

public class CaseSummary {

    private Optional<GroundsForAppeal> groundsForAppeal = Optional.empty();
    private Optional<Issues> issues = Optional.empty();

    public Optional<GroundsForAppeal> getGroundsForAppeal() {
        return groundsForAppeal;
    }

    public Optional<Issues> getIssues() {
        return issues;
    }

    public void setGroundsForAppeal(GroundsForAppeal groundsForAppeal) {
        this.groundsForAppeal = Optional.ofNullable(groundsForAppeal);
    }

    public void setIssues(Issues issues) {
        this.issues = Optional.ofNullable(issues);
    }
}
