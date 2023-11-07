package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_TCW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_UPDATE_HEARING_REQUIRED;

import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class AsylumCaseFieldDefinitionTest {

    @Test
    void mapped_to_equivalent_field_name() {
        Stream.of(AsylumCaseFieldDefinition.values())
            // filter out below variable because of CCD defs constrains to edit existing fields
            .filter(val -> !Set.of(
                ATTENDING_TCW,
                CASE_LEVEL_FLAGS,
                MANUAL_CANCEL_HEARINGS_REQUIRED,
                MANUAL_UPDATE_HEARING_REQUIRED
            ).contains(val))
            .forEach(v -> assertThat(UPPER_UNDERSCORE.to(LOWER_CAMEL, v.name()))
                .isEqualTo(v.value()));
    }

}
