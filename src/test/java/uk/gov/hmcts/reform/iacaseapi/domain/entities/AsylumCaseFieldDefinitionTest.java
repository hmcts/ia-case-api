package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.Test;

public class AsylumCaseFieldDefinitionTest {

    @Test
    public void mapped_to_equivalent_field_name() {
        Stream.of(AsylumCaseFieldDefinition.values())
            //This field is a mandated name by CCD for the Share a Case feature
            .filter(asylumCaseFieldDefinition -> asylumCaseFieldDefinition != AsylumCaseFieldDefinition.ORG_LIST_OF_USERS
                                                 && asylumCaseFieldDefinition != AsylumCaseFieldDefinition.SHARED_WITH_COLLEAGUE_ID)
            .forEach(v -> assertThat(UPPER_UNDERSCORE.to(LOWER_CAMEL, v.name()))
                .isEqualTo(v.value()));
    }

}