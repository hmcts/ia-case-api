package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;

import java.time.LocalDate;

@Service
public class DeletionDateProvider {
    private final DateProvider dateProvider;
    private final int appealTtlDays;

    public DeletionDateProvider(
        DateProvider dateProvider,
        @Value("${appeal_ttl_days}") int appealTtlDays
    ) {
        this.dateProvider = dateProvider;
        this.appealTtlDays = appealTtlDays;
    }

    public LocalDate getDeletionDate() {
        return dateProvider.now().plusDays(appealTtlDays);
    }
}
