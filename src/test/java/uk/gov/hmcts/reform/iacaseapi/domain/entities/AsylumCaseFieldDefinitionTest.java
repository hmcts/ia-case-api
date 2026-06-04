package uk.gov.hmcts.reform.iacaseapi.domain.entities;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ATTENDING_TCW;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTODIAL_SENTENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DATE_CUSTODIAL_SENTENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DETENTION_REMOVAL_APPELLANT_CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CANCEL_HEARINGS_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_CREATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MANUAL_UPDATE_HEARING_REQUIRED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PRISON_NOMS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PRISON_NOMS_AO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REASON_APPELLANT_WAS_DETAINED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_THE_NOTICE_OF_DECISION_DOCS_REHYDRATED;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class AsylumCaseFieldDefinitionTest {

    List<AsylumCaseFieldDefinition> exceptions = List.of(new AsylumCaseFieldDefinition[]{
        ATTENDING_TCW,
        PRISON_NOMS,
        PRISON_NOMS_AO,
        CASE_LEVEL_FLAGS
    });

    @Test
    void mapped_to_equivalent_field_name() {
        Stream.of(AsylumCaseFieldDefinition.values())
            // filter out below variable because of CCD defs constrains to edit existing fields
            .filter(val -> !Set.of(
                ATTENDING_TCW,
                PRISON_NOMS,
                PRISON_NOMS_AO,
                CASE_LEVEL_FLAGS,
                MANUAL_CANCEL_HEARINGS_REQUIRED,
                MANUAL_UPDATE_HEARING_REQUIRED,
                MANUAL_CREATE_HEARING_REQUIRED,
                CUSTODIAL_SENTENCE,
                DATE_CUSTODIAL_SENTENCE,
                REASON_APPELLANT_WAS_DETAINED,
                UPLOAD_THE_NOTICE_OF_DECISION_DOCS_REHYDRATED,
                DETENTION_REMOVAL_APPELLANT_CONTACT_PREFERENCE
            ).contains(val))
            .forEach(v -> assertThat(UPPER_UNDERSCORE.to(LOWER_CAMEL, v.name()))
                .isEqualTo(v.value()));
    }

}
