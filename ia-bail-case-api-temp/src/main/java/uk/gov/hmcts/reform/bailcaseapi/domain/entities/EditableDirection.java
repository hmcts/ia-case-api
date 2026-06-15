package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static java.util.Objects.requireNonNull;

public class EditableDirection {

    private String sendDirectionDescription;
    private String sendDirectionList;
    private String dateOfCompliance;

    private EditableDirection() {
        // noop -- for deserializer
    }

    public EditableDirection(
        String sendDirectionDescription,
        String sendDirectionList,
        String dateOfCompliance
    ) {
        requireNonNull(sendDirectionDescription);
        requireNonNull(sendDirectionList);
        requireNonNull(dateOfCompliance);

        this.sendDirectionDescription = sendDirectionDescription;
        this.sendDirectionList = sendDirectionList;
        this.dateOfCompliance = dateOfCompliance;
    }

    public String getSendDirectionDescription() {
        requireNonNull(sendDirectionDescription);
        return sendDirectionDescription;
    }

    public String getSendDirectionList() {
        requireNonNull(sendDirectionList);
        return sendDirectionList;
    }

    public String getDateOfCompliance() {
        requireNonNull(dateOfCompliance);
        return dateOfCompliance;
    }
}
