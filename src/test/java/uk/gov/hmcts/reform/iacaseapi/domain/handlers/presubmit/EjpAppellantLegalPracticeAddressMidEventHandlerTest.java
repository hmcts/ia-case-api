package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class EjpAppellantLegalPracticeAddressMidEventHandlerTest {

    private static final String NATIONALITIES_PAGE_ID = "appellantNationalities";
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    private EjpAppellantLegalPracticeAddressMidEventHandler ejpAppellantLegalPracticeAddressMidEventHandler;

    @BeforeEach
    public void setUp() {

        ejpAppellantLegalPracticeAddressMidEventHandler = new EjpAppellantLegalPracticeAddressMidEventHandler();
        when(asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class)).thenReturn(Optional.of(SourceOfAppeal.TRANSFERRED_FROM_UPPER_TRIBUNAL));
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(callback.getPageId()).thenReturn(NATIONALITIES_PAGE_ID);

    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "START_APPEAL", "EDIT_APPEAL"
    })
    void should_write_appellantHasFixedAddress_to_yes_for_ejp_unrep(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(asylumCase.read(IS_LEGALLY_REPRESENTED_EJP, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));
        when(asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        PreSubmitCallbackResponse<AsylumCase> response =
            ejpAppellantLegalPracticeAddressMidEventHandler.handle(MID_EVENT, callback);

        assertThat(response).isNotNull();
        verify(asylumCase, times(1)).write(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.YES);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void it_can_handle_callback(Event event) {

        for (PreSubmitCallbackStage stage : PreSubmitCallbackStage.values()) {
            when(callback.getEvent()).thenReturn(event);
            boolean canHandle = ejpAppellantLegalPracticeAddressMidEventHandler.canHandle(stage, callback);

            if (stage == MID_EVENT && (event == Event.START_APPEAL || event == Event.EDIT_APPEAL)) {
                assertTrue(canHandle);
            } else {
                assertFalse(canHandle);
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void should_not_allow_null_arguments(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertThatThrownBy(() -> ejpAppellantLegalPracticeAddressMidEventHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ejpAppellantLegalPracticeAddressMidEventHandler.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ejpAppellantLegalPracticeAddressMidEventHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> ejpAppellantLegalPracticeAddressMidEventHandler.handle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class)
    void handling_should_throw_if_cannot_actually_handle(Event event) {
        when(callback.getEvent()).thenReturn(event);

        assertThatThrownBy(() -> ejpAppellantLegalPracticeAddressMidEventHandler.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> ejpAppellantLegalPracticeAddressMidEventHandler.handle(MID_EVENT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }
}
