package uk.gov.hmcts.reform.iacaseapi.infrastructure.controllers.model;

import lombok.Getter;

@Getter
public class LargeSupplementaryInfoException extends RuntimeException {
    public final transient SupplementaryDetailsResponse supplementaryDetailsResponse;

    public LargeSupplementaryInfoException(SupplementaryDetailsResponse supplementaryDetailsResponse) {
        super();
        this.supplementaryDetailsResponse = supplementaryDetailsResponse;
    }
}
