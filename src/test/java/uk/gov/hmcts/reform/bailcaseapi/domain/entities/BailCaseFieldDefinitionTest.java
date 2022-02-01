package uk.gov.hmcts.reform.bailcaseapi.domain.entities;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

public class BailCaseFieldDefinitionTest {

    @Test
    void mapped_to_correct_field_name() {
        Stream.of(BailCaseFieldDefinition.values())
            .forEach(v -> assertThat(UPPER_UNDERSCORE.to(LOWER_CAMEL, v.name())));
    }
}
