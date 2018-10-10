package uk.gov.hmcts.reform.iacaseapi.shared.domain.entities;

import java.util.Optional;

public class CaseArgument {

    private Optional<GroundsOfAppeal> groundsOfAppeal = Optional.empty();
    private Optional<LegalArgument> legalArgument = Optional.empty();
    private Optional<HomeOfficeResponse> homeOfficeResponse = Optional.empty();
    private Optional<DocumentWithMetadata> hearingSummary = Optional.empty();

    public Optional<GroundsOfAppeal> getGroundsOfAppeal() {
        return groundsOfAppeal;
    }

    public Optional<LegalArgument> getLegalArgument() {
        return legalArgument;
    }

    public Optional<HomeOfficeResponse> getHomeOfficeResponse() {
        return homeOfficeResponse;
    }

    public Optional<DocumentWithMetadata> getHearingSummary() {
        return hearingSummary;
    }

    public void setGroundsOfAppeal(GroundsOfAppeal groundsOfAppeal) {
        this.groundsOfAppeal = Optional.ofNullable(groundsOfAppeal);
    }

    public void setLegalArgument(LegalArgument legalArgument) {
        this.legalArgument = Optional.ofNullable(legalArgument);
    }

    public void setHomeOfficeResponse(HomeOfficeResponse homeOfficeResponse) {
        this.homeOfficeResponse = Optional.ofNullable(homeOfficeResponse);
    }

    public void setHearingSummary(DocumentWithMetadata hearingSummary) {
        this.hearingSummary = Optional.ofNullable(hearingSummary);
    }
}
