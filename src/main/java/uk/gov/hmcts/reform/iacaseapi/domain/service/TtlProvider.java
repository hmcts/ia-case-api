package uk.gov.hmcts.reform.iacaseapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;

import java.time.temporal.TemporalUnit;

@Service
public class TtlProvider {
    private final DateProvider dateProvider;

    public TtlProvider(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

    public String getTtl() {
        return dateProvider.now().plus(1000, TemporalUnit.).toString();
    }
}
