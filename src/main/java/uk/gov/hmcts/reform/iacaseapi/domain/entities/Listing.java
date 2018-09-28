package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import java.util.Optional;

public class Listing {

    private Optional<String> hearingCentre = Optional.empty();
    private Optional<String> hearingLength = Optional.empty();
    private Optional<String> hearingDate = Optional.empty();

    private Listing() {
        // noop -- for deserializer
    }

    public Listing(
        String hearingCentre,
        String hearingLength,
        String hearingDate
    ) {
        this.hearingCentre = Optional.ofNullable(hearingCentre);
        this.hearingLength = Optional.ofNullable(hearingLength);
        this.hearingDate = Optional.ofNullable(hearingDate);
    }

    public Optional<String> getHearingCentre() {
        return hearingCentre;
    }

    public Optional<String> getHearingLength() {
        return hearingLength;
    }

    public Optional<String> getHearingDate() {
        return hearingDate;
    }

    public void setHearingCentre(String hearingCentre) {
        this.hearingCentre = Optional.ofNullable(hearingCentre);
    }

    public void setHearingLength(String hearingLength) {
        this.hearingLength = Optional.ofNullable(hearingLength);
    }

    public void setHearingDate(String hearingDate) {
        this.hearingDate = Optional.ofNullable(hearingDate);
    }
}
