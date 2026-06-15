package uk.gov.hmcts.reform.bailcaseapi.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DateProvider {

    LocalDate now();

    LocalDateTime nowWithTime();
}
