package uk.gov.hmcts.reform.iacaseapi.domain.entities.homeoffice;

import java.util.List;

public class HomeOfficeSearchResponse {

    private MessageHeader messageHeader;
    private String messageType;
    private List<HomeOfficeCaseStatus> status;
    private HomeOfficeError errorDetail;

    private HomeOfficeSearchResponse() {
    }

    public HomeOfficeSearchResponse(MessageHeader messageHeader,
                                    String messageType,
                                    List<HomeOfficeCaseStatus> status,
                                    HomeOfficeError errorDetail) {
        this.messageHeader = messageHeader;
        this.messageType = messageType;
        this.status = status;
        this.errorDetail = errorDetail;
    }

    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    public String getMessageType() {
        return messageType;
    }

    public List<HomeOfficeCaseStatus> getStatus() {
        return status;
    }

    public HomeOfficeError getErrorDetail() {
        return errorDetail;
    }
}
