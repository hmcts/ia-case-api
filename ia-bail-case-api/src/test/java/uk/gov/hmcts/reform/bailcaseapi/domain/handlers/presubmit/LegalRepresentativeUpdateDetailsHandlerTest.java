package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;


@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class LegalRepresentativeUpdateDetailsHandlerTest {

    private final String legalRepCompany = "Company Orange";
    private final String legalRepName = "John Doe";
    private final String legalRepFamilyName = "Jones";
    private final String legalRepEmailAddress = "john.doe@example.com";
    private final String legalRepPhoneNumber = "01234567891";
    private final String legalRepReferenceNumber = "ABC-123";
    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;

    private LegalRepresentativeUpdateDetailsHandler legalRepresentativeUpdateDetailsHandler;

    @BeforeEach
    public void setUp() {
        legalRepresentativeUpdateDetailsHandler = new LegalRepresentativeUpdateDetailsHandler();

        when(callback.getEvent()).thenReturn(Event.UPDATE_BAIL_LEGAL_REP_DETAILS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

        when(bailCase.read(UPDATE_LEGAL_REP_COMPANY, String.class)).thenReturn(Optional.of(legalRepCompany));
        when(bailCase.read(UPDATE_LEGAL_REP_NAME, String.class)).thenReturn(Optional.of(legalRepName));
        when(bailCase.read(UPDATE_LEGAL_REP_FAMILY_NAME, String.class)).thenReturn(Optional.of(legalRepFamilyName));
        when(bailCase.read(UPDATE_LEGAL_REP_EMAIL_ADDRESS, String.class)).thenReturn(Optional.of(legalRepEmailAddress));
        when(bailCase.read(UPDATE_LEGAL_REP_PHONE, String.class)).thenReturn(Optional.of(legalRepPhoneNumber));
        when(bailCase.read(UPDATE_LEGAL_REP_REFERENCE, String.class)).thenReturn(Optional.of(legalRepReferenceNumber));
        when(bailCase.read(IS_LEGALLY_REPRESENTED_FOR_FLAG, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
    }

    @Test
    void should_update_existing_legal_rep_details() {
        PreSubmitCallbackResponse<BailCase> callbackResponse =
            legalRepresentativeUpdateDetailsHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        assertNotNull(callbackResponse);
        assertEquals(bailCase, callbackResponse.getData());

        verify(bailCase).read(UPDATE_LEGAL_REP_COMPANY, String.class);
        verify(bailCase).read(UPDATE_LEGAL_REP_NAME, String.class);
        verify(bailCase).read(UPDATE_LEGAL_REP_FAMILY_NAME, String.class);
        verify(bailCase).read(UPDATE_LEGAL_REP_EMAIL_ADDRESS, String.class);
        verify(bailCase).read(UPDATE_LEGAL_REP_PHONE, String.class);
        verify(bailCase).read(UPDATE_LEGAL_REP_REFERENCE, String.class);

        verify(bailCase, times(1)).clear(eq(UPDATE_LEGAL_REP_COMPANY));
        verify(bailCase, times(1)).clear(eq(UPDATE_LEGAL_REP_NAME));
        verify(bailCase, times(1)).clear(eq(UPDATE_LEGAL_REP_FAMILY_NAME));
        verify(bailCase, times(1)).clear(eq(UPDATE_LEGAL_REP_EMAIL_ADDRESS));
        verify(bailCase, times(1)).clear(eq(UPDATE_LEGAL_REP_PHONE));
        verify(bailCase, times(1)).clear(eq(UPDATE_LEGAL_REP_REFERENCE));

        verify(bailCase, times(1)).write(eq(LEGAL_REP_COMPANY), eq(legalRepCompany));
        verify(bailCase, times(1)).write(eq(LEGAL_REP_NAME), eq(legalRepName));
        verify(bailCase, times(1)).write(eq(LEGAL_REP_FAMILY_NAME), eq(legalRepFamilyName));
        verify(bailCase, times(1)).write(eq(LEGAL_REP_EMAIL_ADDRESS), eq(legalRepEmailAddress));
        verify(bailCase, times(1)).write(eq(LEGAL_REP_PHONE), eq(legalRepPhoneNumber));
        verify(bailCase, times(1)).write(eq(LEGAL_REP_REFERENCE), eq(legalRepReferenceNumber));
        verify(bailCase, times(1)).write(eq(IS_LEGALLY_REPRESENTED_FOR_FLAG), eq(YesOrNo.YES));

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> legalRepresentativeUpdateDetailsHandler
            .canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepresentativeUpdateDetailsHandler
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);


        assertThatThrownBy(() -> legalRepresentativeUpdateDetailsHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        assertThatThrownBy(() -> legalRepresentativeUpdateDetailsHandler
            .handle(ABOUT_TO_START, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = legalRepresentativeUpdateDetailsHandler.canHandle(callbackStage, callback);

                if (event == Event.UPDATE_BAIL_LEGAL_REP_DETAILS
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> legalRepresentativeUpdateDetailsHandler
            .canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> legalRepresentativeUpdateDetailsHandler
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
