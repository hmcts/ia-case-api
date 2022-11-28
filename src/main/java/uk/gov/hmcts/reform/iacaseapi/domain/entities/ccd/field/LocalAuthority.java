package uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class LocalAuthority {

    @JsonProperty("localAuthorityCode")
    private Optional<String> localAuthorityCode = Optional.empty();
    @JsonProperty("checklistNoLocalAuthority")
    private Optional<String> checklistNoLocalAuthority = Optional.empty();
    @JsonProperty("localAuthorityAddress")
    private Optional<AddressGlobal> localAuthorityAddress = Optional.empty();

}
