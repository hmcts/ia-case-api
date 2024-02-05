package uk.gov.hmcts.reform.iacaseapi.infrastructure;

import org.springframework.retry.RetryContext;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;

@Service
public class RetryCounter {
    public RetryContext getContext() {
        return RetrySynchronizationManager.getContext();
    }
}
