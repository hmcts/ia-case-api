package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Optional;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CcdEventPostSubmitResponse {

    private Optional<String> confirmationHeader = Optional.empty();
    private Optional<String> confirmationBody = Optional.empty();

    public Optional<String> getConfirmationHeader() {
        return confirmationHeader;
    }

    public Optional<String> getConfirmationBody() {
        return confirmationBody;
    }

    public void setConfirmationHeader(String confirmationHeader) {
        this.confirmationHeader = Optional.ofNullable(confirmationHeader);
    }

    public void setConfirmationBody(String confirmationBody) {
        this.confirmationBody = Optional.ofNullable(confirmationBody);
    }
}
