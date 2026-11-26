package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@ExtendWith(MockitoExtension.class)
class CurrentFtpaDecisionResolverTest {

    @Mock
    private AsylumCase asylumCase;

    private final CurrentFtpaDecisionResolver resolver = new CurrentFtpaDecisionResolver();

    @Test
    void should_return_valid_applicant_type_only() {
        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("respondent"));
        assertThat(resolver.getApplicantType(asylumCase)).contains("respondent");

        when(asylumCase.read(FTPA_APPLICANT_TYPE, String.class)).thenReturn(Optional.of("other"));
        assertThat(resolver.getApplicantType(asylumCase)).isEmpty();
    }

    @Test
    void should_check_if_decided() {
        when(asylumCase.read(IS_FTPA_APPELLANT_DECIDED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(asylumCase.read(IS_FTPA_RESPONDENT_DECIDED, YesOrNo.class)).thenReturn(Optional.empty());

        assertThat(resolver.isDecided(asylumCase, "appellant")).isTrue();
        assertThat(resolver.isDecided(asylumCase, "respondent")).isFalse();
    }

    @Test
    void should_use_resident_judge_outcome_field_when_present() {
        when(asylumCase.read(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE, String.class))
            .thenReturn(Optional.of("granted"));

        assertThat(resolver.getCurrentOutcomeField(asylumCase, "appellant"))
            .isEqualTo(FTPA_APPELLANT_RJ_DECISION_OUTCOME_TYPE);
        assertThat(resolver.getCurrentOutcome(asylumCase, "appellant")).contains("granted");
    }

    @Test
    void should_fall_back_to_legacy_outcome_field() {
        when(asylumCase.read(FTPA_RESPONDENT_RJ_DECISION_OUTCOME_TYPE, String.class)).thenReturn(Optional.empty());

        assertThat(resolver.getCurrentOutcomeField(asylumCase, "respondent"))
            .isEqualTo(FTPA_RESPONDENT_DECISION_OUTCOME_TYPE);
    }

    @Test
    void should_check_decision_changed_and_document_uploaded() {
        when(asylumCase.read(FTPA_DECISION_UPDATED_CONFIRMATION, String.class))
            .thenReturn(Optional.of(CurrentFtpaDecisionResolver.DECISION_UPDATED));
        when(asylumCase.read(UPDATE_FTPA_DECISION_AND_REASONS_FINAL_CHECK, YesOrNo.class))
            .thenReturn(Optional.of(YesOrNo.NO));

        assertThat(resolver.isDecisionChanged(asylumCase)).isTrue();
        assertThat(resolver.isDocumentUploaded(asylumCase)).isFalse();
    }

    @Test
    void should_clear_update_fields() {
        resolver.clearUpdateFtpaDecisionFields(asylumCase);

        verify(asylumCase).clear(UPDATE_FTPA_DECISION_LIST);
        verify(asylumCase).clear(UPDATE_FTPA_CURRENT_DECISION_TEXT);
        verify(asylumCase).clear(FTPA_DECISION_UPDATED_CONFIRMATION);
        verify(asylumCase).clear(UPDATE_FTPA_DECISION_OUTCOME_TYPE);
        verify(asylumCase).clear(UPDATE_FTPA_DECISION_AND_REASONS_FINAL_CHECK);
        verify(asylumCase).clear(FTPA_DECISION_AND_REASONS_DOC_UPLOAD);
        verify(asylumCase).clear(SUMMARISE_FTPA_DECISION_AND_REASONS_DOCUMENT);
    }
}
