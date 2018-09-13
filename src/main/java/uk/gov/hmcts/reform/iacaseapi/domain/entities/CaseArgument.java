package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;

public class CaseArgument {

    private Optional<GroundsForAppeal> groundsForAppeal = Optional.empty();
    private Optional<WrittenLegalArgument> writtenLegalArgument = Optional.empty();

    public Optional<GroundsForAppeal> getGroundsForAppeal() {
        return groundsForAppeal;
    }

    public Optional<WrittenLegalArgument> getWrittenLegalArgument() {
        return writtenLegalArgument;
    }

    public void setGroundsForAppeal(GroundsForAppeal groundsForAppeal) {
        this.groundsForAppeal = Optional.ofNullable(groundsForAppeal);
    }

    public void setWrittenLegalArgument(WrittenLegalArgument writtenLegalArgument) {
        this.writtenLegalArgument = Optional.ofNullable(writtenLegalArgument);
    }
}
