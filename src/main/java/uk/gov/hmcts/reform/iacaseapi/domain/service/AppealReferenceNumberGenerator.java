package uk.gov.hmcts.reform.iacaseapi.domain.service;

import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;

public interface AppealReferenceNumberGenerator {

    String generate(
        long caseId,
        AppealType appealType
    );

    boolean referenceNumberExists(String referenceNumber);

    /**
     * Registers a manually entered reference number in the database.
     * This ensures duplicate checking works for both generated and manually entered reference numbers.
     *
     * @param caseId The case ID
     * @param referenceNumber The reference number in format XX/00000/0000
     */
    void registerReferenceNumber(long caseId, String referenceNumber);

}
