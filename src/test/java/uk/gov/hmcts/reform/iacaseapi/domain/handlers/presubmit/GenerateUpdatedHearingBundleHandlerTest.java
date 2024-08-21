package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class GenerateUpdatedHearingBundleHandlerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;

    private final GenerateUpdatedHearingBundleHandler generateUpdatedHearingBundleHandler =
        new GenerateUpdatedHearingBundleHandler();

    @Test
    void should_handle_for_generate_updated_hearing_bundle_event() {
        when(callback.getEvent()).thenReturn(Event.GENERATE_UPDATED_HEARING_BUNDLE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        generateUpdatedHearingBundleHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1))
            .write(AsylumCaseFieldDefinition.IS_HEARING_BUNDLE_UPDATED, YesOrNo.YES);
        verify(asylumCase, times(1))
            .read(HEARING_CENTRE, HearingCentre.class);
        verify(asylumCase, times(0))
            .write(eq(AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE), any());
    }

    @Test
    void should_throw_error_if_cannot_handle_callback() {
        when(callback.getEvent()).thenReturn(Event.GENERATE_UPDATED_HEARING_BUNDLE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);

        assertThatThrownBy(() -> generateUpdatedHearingBundleHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        verify(asylumCase, times(0))
            .write(AsylumCaseFieldDefinition.IS_HEARING_BUNDLE_UPDATED, YesOrNo.YES);
    }

    @Test
    void should_throw_error_if_null_callback_or_callback_stage() {
        assertThatThrownBy(() -> generateUpdatedHearingBundleHandler
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> generateUpdatedHearingBundleHandler
            .handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void should_do_nothing_with_hearing_centre_if_listing_hearing_centre_is_present() {
        when(callback.getEvent()).thenReturn(Event.GENERATE_UPDATED_HEARING_BUNDLE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.BELFAST));

        generateUpdatedHearingBundleHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(asylumCase, times(0))
            .read(HEARING_CENTRE);
        verify(asylumCase, times(0))
            .write(eq(AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE), any());
    }

    @ParameterizedTest
    @EnumSource(value = HearingCentre.class, mode = EXCLUDE, names = {"GLASGOW"})
    void should_set_listing_hearing_centre_to_hearing_centre_if_not_present(HearingCentre hearingCentre) {
        when(callback.getEvent()).thenReturn(Event.GENERATE_UPDATED_HEARING_BUNDLE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(hearingCentre));

        generateUpdatedHearingBundleHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);

        verify(asylumCase, times(1))
            .write(AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE, hearingCentre);
    }

    @Test
    void should_set_listing_hearing_centre_to_hearing_centre_if_not_present_glasgow() {
        when(callback.getEvent()).thenReturn(Event.GENERATE_UPDATED_HEARING_BUNDLE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
        when(asylumCase.read(HEARING_CENTRE, HearingCentre.class)).thenReturn(Optional.of(HearingCentre.GLASGOW));

        generateUpdatedHearingBundleHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback);
        verify(asylumCase, times(0))
            .write(AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.GLASGOW);
        verify(asylumCase, times(1))
            .write(AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE, HearingCentre.GLASGOW_TRIBUNALS_CENTRE);
    }
}
