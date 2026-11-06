package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;

public class PostSubmitCallbackResponse {

    @JsonProperty("confirmation_header")
    private Optional<String> confirmationHeader = Optional.empty();
    @JsonProperty("confirmation_body")
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
