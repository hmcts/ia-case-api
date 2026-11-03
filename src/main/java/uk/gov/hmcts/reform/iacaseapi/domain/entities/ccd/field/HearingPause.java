package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import lombok.*;

@EqualsAndHashCode
@ToString
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HearingPause {
    private String pauseStartTime;
    private String pauseEndTime;
}
