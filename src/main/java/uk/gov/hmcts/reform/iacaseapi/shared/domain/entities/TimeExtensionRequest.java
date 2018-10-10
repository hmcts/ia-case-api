package uk.gov.hmcts.reform.iacaseapi.shared.domain.entities;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.Document;

public class TimeExtensionRequest {

    private Optional<String> direction = Optional.empty();
    private Optional<String> timeRequested = Optional.empty();
    private Optional<String> reason = Optional.empty();
    private Optional<Document> supportingEvidence = Optional.empty();

    private TimeExtensionRequest() {
        // noop -- for deserializer
    }

    public Optional<String> getDirection() {
        return direction;
    }

    public Optional<String> getTimeRequested() {
        return timeRequested;
    }

    public Optional<String> getReason() {
        return reason;
    }

    public Optional<Document> getSupportingEvidence() {
        return supportingEvidence;
    }

    public void setDirection(String direction) {
        this.direction = Optional.ofNullable(direction);
    }

    public void setTimeRequested(String timeRequested) {
        this.timeRequested = Optional.ofNullable(timeRequested);
    }

    public void setReason(String reason) {
        this.reason = Optional.ofNullable(reason);
    }

    public void setSupportingEvidence(Document supportingEvidence) {
        this.supportingEvidence = Optional.ofNullable(supportingEvidence);
    }
}
