package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_NOT_SUBMITTED_REASON_DOCUMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_WAS_NOT_SUBMITTED_REASON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANTS_REPRESENTATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.EMAIL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_ADDED_LEGAL_REP_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.INTERNAL_APPELLANT_EMAIL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.INTERNAL_APPELLANT_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_ADDRESS_U_K;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_PAPER_J;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_EMAIL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_FAMILY_NAME_PAPER_J;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_GIVEN_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_HAS_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_REF_NUMBER_PAPER_J;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OOC_ADDRESS_LINE_1;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OOC_ADDRESS_LINE_2;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OOC_ADDRESS_LINE_3;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OOC_ADDRESS_LINE_4;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OOC_COUNTRY_LINE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OOC_LR_COUNTRY_GOV_UK_ADMIN_J;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class InternalCaseCreationContactDetailsAppenderTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private String internalMobileNumber = "07123123123";

    private String internalEmailAddress = "test_email@domain.com";

    private InternalCaseCreationContactDetailsAppender internalCaseCreationContactDetailsAppender;

    @BeforeEach
    public void setUp() {

        internalCaseCreationContactDetailsAppender = new InternalCaseCreationContactDetailsAppender();
        when(callback.getEvent()).thenReturn(START_APPEAL);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(IS_ADMIN, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

    }

    @ParameterizedTest
    @EnumSource(value = YesOrNo.class, names = {"NO", "YES"})
    void handler_checks_internal_contact_details_and_appends_to_main_fields_and_clears_legal_rep_details(
            YesOrNo isAppellantsRepresentation
    ) {

        when(asylumCase.read(INTERNAL_APPELLANT_MOBILE_NUMBER, String.class)).thenReturn(Optional.of(internalMobileNumber));
        when(asylumCase.read(INTERNAL_APPELLANT_EMAIL, String.class)).thenReturn(Optional.of(internalEmailAddress));
        when(asylumCase.read(APPELLANTS_REPRESENTATION, YesOrNo.class)).thenReturn(Optional.of(isAppellantsRepresentation));

        PreSubmitCallbackResponse<AsylumCase> response =
                internalCaseCreationContactDetailsAppender.handle(ABOUT_TO_SUBMIT, callback);

        assertThat(response).isNotNull();

        verify(asylumCase, times(1)).write(MOBILE_NUMBER, Optional.of(internalMobileNumber));
        verify(asylumCase, times(1)).write(EMAIL, Optional.of(internalEmailAddress));

        if (isAppellantsRepresentation == YES) {
            verify(asylumCase).write(HAS_ADDED_LEGAL_REP_DETAILS, NO);
            verify(asylumCase).clear(APPEAL_WAS_NOT_SUBMITTED_REASON);
            verify(asylumCase).clear(APPEAL_NOT_SUBMITTED_REASON_DOCUMENTS);
            verify(asylumCase).clear(LEGAL_REP_COMPANY_PAPER_J);
            verify(asylumCase).clear(LEGAL_REP_GIVEN_NAME);
            verify(asylumCase).clear(LEGAL_REP_FAMILY_NAME_PAPER_J);
            verify(asylumCase).clear(LEGAL_REP_EMAIL);
            verify(asylumCase).clear(LEGAL_REP_REF_NUMBER_PAPER_J);
            verify(asylumCase).clear(LEGAL_REP_ADDRESS_U_K);
            verify(asylumCase).clear(OOC_ADDRESS_LINE_1);
            verify(asylumCase).clear(OOC_ADDRESS_LINE_2);
            verify(asylumCase).clear(OOC_ADDRESS_LINE_3);
            verify(asylumCase).clear(OOC_ADDRESS_LINE_4);
            verify(asylumCase).clear(OOC_COUNTRY_LINE);
            verify(asylumCase).clear(OOC_LR_COUNTRY_GOV_UK_ADMIN_J);
            verify(asylumCase).clear(LEGAL_REP_HAS_ADDRESS);
        } else {
            verify(asylumCase, never()).clear();
        }
    }

    @Test
    void it_can_handle_callback_for_all_events() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = internalCaseCreationContactDetailsAppender.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && (callback.getEvent() == Event.START_APPEAL || callback.getEvent() == Event.EDIT_APPEAL)) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> internalCaseCreationContactDetailsAppender.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalCaseCreationContactDetailsAppender.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalCaseCreationContactDetailsAppender.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> internalCaseCreationContactDetailsAppender.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }
}