package uk.gov.hmcts.reform.iacaseapi.domain;

import java.time.LocalDate;

public interface DateProvider {

    LocalDate now();
}
