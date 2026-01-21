package uk.gov.hmcts.reform.bailcaseapi.component.testutils.fixtures;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Optional;

@Getter
public class PostSubmitCallbackResponseForTest {

    @JsonProperty
    private Optional<String> confirmationBody;
    @JsonProperty
    private Optional<String> confirmationHeader;

    @JsonCreator
    public PostSubmitCallbackResponseForTest(
        @JsonProperty("confirmation_header") String header,
        @JsonProperty("confirmation_body") String body
    ) {
        this.confirmationHeader = Optional.of(header);
        this.confirmationBody = Optional.of(body);
    }

    public static class PostSubmitCallbackResponseForTestBuilder implements Builder<PostSubmitCallbackResponseForTest> {

        public static PostSubmitCallbackResponseForTestBuilder somePostSubmittedCallbackResponseWith() {
            return new PostSubmitCallbackResponseForTestBuilder();
        }

        private String confirmationHeader;
        private String confirmationBody;

        public PostSubmitCallbackResponseForTestBuilder confirmationHeader(String confirmationHeader) {
            this.confirmationHeader = confirmationHeader;
            return this;
        }

        public PostSubmitCallbackResponseForTestBuilder confirmationBody(String confirmationBody) {
            this.confirmationBody = confirmationBody;
            return this;
        }

        public PostSubmitCallbackResponseForTest build() {
            return new PostSubmitCallbackResponseForTest(confirmationHeader, confirmationBody);
        }
    }
}
