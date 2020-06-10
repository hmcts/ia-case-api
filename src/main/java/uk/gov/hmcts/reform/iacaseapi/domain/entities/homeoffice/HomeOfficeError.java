package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

public class HomeOfficeError {

    private String errorCode;
    private String messageText;
    private boolean success;

    private HomeOfficeError() {
    }

    public HomeOfficeError(String errorCode, String messageText, boolean success) {
        this.errorCode = errorCode;
        this.messageText = messageText;
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessageText() {
        return messageText;
    }

    public boolean isSuccess() {
        return success;
    }
}
