package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model;

import lombok.Getter;

@Getter
public class EmptySupplementaryInfoException extends RuntimeException {
    public transient final SupplementaryDetailsResponse supplementaryDetailsResponse;

    public EmptySupplementaryInfoException(SupplementaryDetailsResponse supplementaryDetailsResponse) {
        super();
        this.supplementaryDetailsResponse = supplementaryDetailsResponse;
    }
}
