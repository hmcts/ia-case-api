package uk.gov.hmcts.reform.iacaseapi.domain.entities.commondata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import nonapi.io.github.classgraph.json.Id;

@Data
public class ListOfValue {

    @Id
    @JsonIgnore
    private String id;
    private String key;
    private String value;
}
