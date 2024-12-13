package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;

import java.time.LocalDate;

@Service
public class TtlProvider {
    private final DateProvider dateProvider;
    private final int appealDraftTtlDays;

    public TtlProvider(
            DateProvider dateProvider,
            @Value("${appealDraftTtlDays}") int appealDraftTtlDays
    ) {
        this.dateProvider = dateProvider;
        this.appealDraftTtlDays = appealDraftTtlDays;
    }

    public LocalDate getTtl() {
        return dateProvider.now().plusDays(appealDraftTtlDays);
    }
}
