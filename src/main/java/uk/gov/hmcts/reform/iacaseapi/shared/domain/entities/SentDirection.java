package uk.gov.hmcts.reform.iacaseapi.shared.domain.entities;

import java.util.Optional;

public class SentDirection extends Direction {

    private Optional<String> revisedDueDate = Optional.empty();
    private Optional<String> dateSent = Optional.empty();
    private Optional<String> status = Optional.empty();

    private SentDirection() {
        // noop -- for deserializer
    }

    public SentDirection(
        Direction direction,
        String dateSent,
        String status
    ) {
        this.setDirection(direction.getDirection().orElse(null));
        this.setDescription(direction.getDescription().orElse(null));
        this.setParties(direction.getParties().orElse(null));
        this.setDueDate(direction.getDueDate().orElse(null));
        this.dateSent = Optional.of(dateSent);
        this.status = Optional.of(status);
    }

    public Optional<String> getRevisedDueDate() {
        return revisedDueDate;
    }

    public Optional<String> getDateSent() {
        return dateSent;
    }

    public Optional<String> getStatus() {
        return status;
    }

    public void markAsComplete() {
        setStatus("Complete");
    }

    public void setRevisedDueDate(String revisedDueDate) {
        this.revisedDueDate = Optional.ofNullable(revisedDueDate);
    }

    public void setDateSent(String dateSent) {
        this.dateSent = Optional.ofNullable(dateSent);
    }

    public void setStatus(String status) {
        this.status = Optional.ofNullable(status);
    }
}
