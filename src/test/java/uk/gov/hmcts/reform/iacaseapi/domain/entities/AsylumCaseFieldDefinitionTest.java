package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static com.google.common.base.CaseFormat.*;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_TCW;

import java.util.stream.Stream;
import org.junit.Test;

public class AsylumCaseFieldDefinitionTest {

    @Test
    public void mapped_to_equivalent_field_name() {
        Stream.of(AsylumCaseFieldDefinition.values())
            // filter out below variable because of CCD defs constrains to edit existing fields
            .filter(val -> val != ATTENDING_TCW)
            .forEach(v -> assertThat(UPPER_UNDERSCORE.to(LOWER_CAMEL, v.name()))
                .isEqualTo(v.value()));
    }

}
