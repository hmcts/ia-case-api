package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SourceOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class EjpEditAppealHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private EjpEditAppealHandler ejpEditAppealHandler;

    @BeforeEach
    void setup() {
        ejpEditAppealHandler = new EjpEditAppealHandler();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YES));
    }

    @Test
    void should_clear_ejp_legal_rep_fields_when_editing_to_not_repped() {
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.TRANSFERRED_FROM_UPPER_TRIBUNAL));
        when(asylumCase.read(IS_LEGALLY_REPRESENTED_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> response = ejpEditAppealHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).clear(LEGAL_REP_COMPANY_EJP);
        verify(asylumCase, times(1)).clear(LEGAL_REP_GIVEN_NAME_EJP);
        verify(asylumCase, times(1)).clear(LEGAL_REP_FAMILY_NAME_EJP);
        verify(asylumCase, times(1)).clear(LEGAL_REP_EMAIL_EJP);
        verify(asylumCase, times(1)).clear(LEGAL_REP_REFERENCE_EJP);
    }

    @Test
    void should_clear_paper_form_fields_when_editing_to_ejp_case() {
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.TRANSFERRED_FROM_UPPER_TRIBUNAL));

        PreSubmitCallbackResponse<AsylumCase> response = ejpEditAppealHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).clear(TRIBUNAL_RECEIVED_DATE);
        verify(asylumCase, times(1)).clear(HOME_OFFICE_DECISION_DATE);
        verify(asylumCase, times(1)).clear(APPELLANT_IN_UK);
        verify(asylumCase, times(1)).clear(IS_ACCELERATED_DETAINED_APPEAL);
        verify(asylumCase, times(1)).clear(DECISION_HEARING_FEE_OPTION);
        verify(asylumCase, times(1)).clear(UPLOAD_THE_APPEAL_FORM_DOCS);
    }

    @Test
    void should_clear_ejp_fields_when_editing_to_paper_form_case() {
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.PAPER_FORM));

        PreSubmitCallbackResponse<AsylumCase> response = ejpEditAppealHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).clear(UPPER_TRIBUNAL_REFERENCE_NUMBER);
        verify(asylumCase, times(1)).clear(FIRST_TIER_TRIBUNAL_TRANSFER_DATE);
        verify(asylumCase, times(1)).clear(STATE_OF_THE_APPEAL);
        verify(asylumCase, times(1)).clear(UT_TRANSFER_DOC);
        verify(asylumCase, times(1)).clear(UPLOAD_EJP_APPEAL_FORM_DOCS);
        verify(asylumCase, times(1)).clear(IS_LEGALLY_REPRESENTED_EJP);
        verify(asylumCase, times(1)).clear(LEGAL_REP_COMPANY_EJP);
        verify(asylumCase, times(1)).clear(LEGAL_REP_GIVEN_NAME_EJP);
        verify(asylumCase, times(1)).clear(LEGAL_REP_FAMILY_NAME_EJP);
        verify(asylumCase, times(1)).clear(LEGAL_REP_EMAIL_EJP);
        verify(asylumCase, times(1)).clear(LEGAL_REP_REFERENCE_EJP);
    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = { "NO", "YES" })
    void should_clear_ho_letter_sent_field_editing_to_paper_form_non_ada(YesOrNo isAda) {
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.PAPER_FORM));
        when(asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)).thenReturn(Optional.of(isAda));
        when(asylumCase.read(DECISION_LETTER_RECEIVED_DATE, String.class)).thenReturn(Optional.of("2024-02-01"));
        when(asylumCase.read(HOME_OFFICE_DECISION_DATE, String.class)).thenReturn(Optional.of("2024-02-01"));

        PreSubmitCallbackResponse<AsylumCase> response = ejpEditAppealHandler.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();
        if (isAda.equals(NO)) {
            verify(asylumCase, times(1)).clear(DECISION_LETTER_RECEIVED_DATE);
        } else {
            verify(asylumCase, times(1)).clear(HOME_OFFICE_DECISION_DATE);
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> ejpEditAppealHandler.handle(MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> ejpEditAppealHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}