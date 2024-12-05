package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;

import java.time.LocalDate;

@Service
public class DeletionDateProvider {
    private final DateProvider dateProvider;
    private final int appealDraftTtlDays;

    public DeletionDateProvider(
        DateProvider dateProvider,
        @Value("${appealDraftTtlDays}") int appealDraftTtlDays
    ) {
        this.dateProvider = dateProvider;
        this.appealDraftTtlDays = appealDraftTtlDays;
    }

    public LocalDate getDeletionDate() {
        return dateProvider.now().plusDays(appealDraftTtlDays);
    }
}
