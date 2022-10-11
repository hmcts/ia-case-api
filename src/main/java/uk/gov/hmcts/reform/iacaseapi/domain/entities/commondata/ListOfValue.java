package uk.gov.hmcts.reform.iacaseapi.domain.entities.commondata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import lombok.Data;

@Data
@Entity
public class ListOfValue {

    @Id
    @Column(name = "ctid")
    @JsonIgnore
    private String id;
    private String key;
    private String value;
}
