package uk.gov.hmcts.reform.iacaseapi.shared.domain.entities;

import java.util.Optional;
import uk.gov.hmcts.reform.iacaseapi.shared.domain.entities.ccd.Document;

public class TimeExtension {

    private Optional<String> dateRequested = Optional.empty();
    private Optional<String> direction = Optional.empty();
    private Optional<String> timeRequested = Optional.empty();
    private Optional<String> reason = Optional.empty();
    private Optional<Document> supportingEvidence = Optional.empty();
    private Optional<String> requestedBy = Optional.empty();
    private Optional<String> status = Optional.empty();
    private Optional<String> comment = Optional.empty();

    public Optional<String> getDateRequested() {
        return dateRequested;
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

    public Optional<String> getRequestedBy() {
        return requestedBy;
    }

    public Optional<String> getStatus() {
        return status;
    }

    public Optional<String> getComment() {
        return comment;
    }

    public void setDirection(String direction) {
        this.direction = Optional.ofNullable(direction);
    }

    public void setDateRequested(String dateRequested) {
        this.dateRequested = Optional.ofNullable(dateRequested);
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

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = Optional.ofNullable(requestedBy);
    }

    public void setStatus(String status) {
        this.status = Optional.ofNullable(status);
    }

    public void setComment(String comment) {
        this.comment = Optional.ofNullable(comment);
    }
}
