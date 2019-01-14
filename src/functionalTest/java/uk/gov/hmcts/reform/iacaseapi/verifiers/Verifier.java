package uk.gov.hmcts.reform.iacaseapi.verifiers;

import java.util.Map;

public interface Verifier {

    void verify(
        long testCaseId,
        Map<String, Object> scenario,
        Map<String, Object> expectedResponse,
        Map<String, Object> actualResponse
    );
}
