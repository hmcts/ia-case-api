package uk.gov.hmcts.reform.iacaseapi.domain.entities.commondata;

import java.util.List;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class CaseFlagDto {
    private List<Flag> flags;
}
