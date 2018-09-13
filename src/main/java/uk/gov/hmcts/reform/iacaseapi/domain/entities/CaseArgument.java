package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;

public class CaseArgument {

    private Optional<GroundsForAppeal> groundsForAppeal = Optional.empty();
    private Optional<WrittenLegalArgument> writtenLegalArgument = Optional.empty();
    private Optional<HomeOfficeResponse> homeOfficeResponse = Optional.empty();
    private Optional<HearingSummary> hearingSummary = Optional.empty();

    public Optional<GroundsForAppeal> getGroundsForAppeal() {
        return groundsForAppeal;
    }

    public Optional<WrittenLegalArgument> getWrittenLegalArgument() {
        return writtenLegalArgument;
    }

    public Optional<HomeOfficeResponse> getHomeOfficeResponse() {
        return homeOfficeResponse;
    }

    public Optional<HearingSummary> getHearingSummary() {
        return hearingSummary;
    }

    public void setGroundsForAppeal(GroundsForAppeal groundsForAppeal) {
        this.groundsForAppeal = Optional.ofNullable(groundsForAppeal);
    }

    public void setWrittenLegalArgument(WrittenLegalArgument writtenLegalArgument) {
        this.writtenLegalArgument = Optional.ofNullable(writtenLegalArgument);
    }

    public void setHomeOfficeResponse(HomeOfficeResponse homeOfficeResponse) {
        this.homeOfficeResponse = Optional.ofNullable(homeOfficeResponse);
    }

    public void setHearingSummary(HearingSummary hearingSummary) {
        this.hearingSummary = Optional.ofNullable(hearingSummary);
    }
}
