package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;

public class CaseArgument {

    private Optional<GroundsOfAppeal> groundsOfAppeal = Optional.empty();
    private Optional<WrittenLegalArgument> writtenLegalArgument = Optional.empty();
    private Optional<HomeOfficeResponse> homeOfficeResponse = Optional.empty();
    private Optional<HearingSummary> hearingSummary = Optional.empty();

    public Optional<GroundsOfAppeal> getGroundsOfAppeal() {
        return groundsOfAppeal;
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

    public void setGroundsOfAppeal(GroundsOfAppeal groundsOfAppeal) {
        this.groundsOfAppeal = Optional.ofNullable(groundsOfAppeal);
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
