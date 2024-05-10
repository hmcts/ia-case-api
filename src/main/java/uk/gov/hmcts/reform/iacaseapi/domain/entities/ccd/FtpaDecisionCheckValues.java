package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class FtpaDecisionCheckValues<T> {

    private List<T> specialDifficulty;
    private List<T> specialReasons;
    private List<T> countryGuidance;

    private FtpaDecisionCheckValues() {
        // noop -- for deserializer
    }
}
