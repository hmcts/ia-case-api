package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SourceOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
public class InCountryToOutOfCountryHandlerTest {
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private InCountryToOutOfCountryHandler inCountryToOutOfCountryHandler;

    @BeforeEach
    public void setUp() {
        inCountryToOutOfCountryHandler = new InCountryToOutOfCountryHandler();

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getPageId()).thenReturn("outOfCountry");
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.empty());
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void it_can_handle_callback(Event event) {

        for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
            when(callback.getEvent()).thenReturn(event);
            boolean canHandle = inCountryToOutOfCountryHandler.canHandle(stage, callback);

            if (stage == MID_EVENT && (event == Event.START_APPEAL || event == Event.EDIT_APPEAL)) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
        }
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> inCountryToOutOfCountryHandler.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> inCountryToOutOfCountryHandler.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> inCountryToOutOfCountryHandler.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> inCountryToOutOfCountryHandler.canHandle(MID_EVENT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"START_APPEAL", "EDIT_APPEAL"})
    void should_throw_if_appellant_in_uk_field_not_present(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertThatThrownBy(() -> inCountryToOutOfCountryHandler.handle(MID_EVENT, callback))
                .hasMessage("Unable to determine if appeal is in UK or out of country")
                .isExactlyInstanceOf(IllegalStateException.class);

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"START_APPEAL", "EDIT_APPEAL"})
    void should_clear_in_country_fields_when_moving_to_out_of_country(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.NO));

        inCountryToOutOfCountryHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(asylumCase, Mockito.times(1)).write(APPELLANT_IN_DETENTION, YesOrNo.NO);
        verify(asylumCase, Mockito.times(1)).write(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.NO);
        verify(asylumCase, Mockito.times(1)).clear(DETENTION_FACILITY);
        verify(asylumCase, Mockito.times(1)).clear(DETENTION_STATUS);
        verify(asylumCase, Mockito.times(1)).clear(CUSTODIAL_SENTENCE);
        verify(asylumCase, Mockito.times(1)).clear(IRC_NAME);
        verify(asylumCase, Mockito.times(1)).clear(PRISON_NAME);

        verify(asylumCase, never()).clear(OUT_OF_COUNTRY_DECISION_TYPE);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {"START_APPEAL", "EDIT_APPEAL"})
    void should_clear_ooc_fields_when_moving_to_uk_appeal(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(APPELLANT_IN_UK, YesOrNo.class))
                .thenReturn(Optional.of(YesOrNo.YES));

        inCountryToOutOfCountryHandler.handle(PreSubmitCallbackStage.MID_EVENT, callback);

        verify(asylumCase, times(1)).clear(OUT_OF_COUNTRY_DECISION_TYPE);

        verify(asylumCase, never()).write(APPELLANT_IN_DETENTION, YesOrNo.NO);
        verify(asylumCase, never()).write(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.NO);
        verify(asylumCase, never()).clear(DETENTION_FACILITY);
        verify(asylumCase, never()).clear(DETENTION_STATUS);
        verify(asylumCase, never()).clear(CUSTODIAL_SENTENCE);
        verify(asylumCase, never()).clear(IRC_NAME);
        verify(asylumCase, never()).clear(PRISON_NAME);
    }
}
