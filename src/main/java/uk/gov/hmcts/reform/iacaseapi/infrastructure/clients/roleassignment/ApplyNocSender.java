package uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.roleassignment;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.ApplyNocRetryableExecutor;

@Slf4j
@Service
public class ApplyNocSender {
    private final ApplyNocRetryableExecutor applyNocRetryableExecutor;

    public ApplyNocSender(
        ApplyNocRetryableExecutor applyNocRetryableExecutor
    ) {
        this.applyNocRetryableExecutor = applyNocRetryableExecutor;
    }

    @Async
    public void sendApplyNoc(Callback<AsylumCase> callback) {
        applyNocRetryableExecutor.retryCall(callback);
    }
}
