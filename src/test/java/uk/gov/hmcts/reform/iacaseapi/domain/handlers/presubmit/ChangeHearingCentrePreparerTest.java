package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE_DYNAMIC_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_CASE_USING_LOCATION_REF_DATA;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class ChangeHearingCentrePreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private LocationRefDataService locationRefDataService;
    private ChangeHearingCentrePreparer changeHearingCentrePreparer;

    @BeforeEach
    public void setUp() {
        changeHearingCentrePreparer = new ChangeHearingCentrePreparer(locationRefDataService);
        when(callback.getEvent()).thenReturn(Event.CHANGE_HEARING_CENTRE);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = changeHearingCentrePreparer.canHandle(callbackStage, callback);

                if (callbackStage == PreSubmitCallbackStage.ABOUT_TO_START && event == Event.CHANGE_HEARING_CENTRE) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset();
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> changeHearingCentrePreparer.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeHearingCentrePreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeHearingCentrePreparer.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> changeHearingCentrePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> changeHearingCentrePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.EDIT_APPEAL);
        assertThatThrownBy(() -> changeHearingCentrePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }


    @Test
    void should_update_hearing_centre_dynamic_list_with_ref_data_hearing_venues() {
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        final DynamicList hearingCentreList = new DynamicList(
                new Value("111111", "First hearing center"),
                List.of(new Value("111111", "First hearing center"))
        );
        when(asylumCase.read(HEARING_CENTRE_DYNAMIC_LIST, DynamicList.class))
                .thenReturn(Optional.of(hearingCentreList));

        final DynamicList refDataHearingCentreList = new DynamicList(
                new Value("", ""),
                List.of(
                        new Value("111111", "First hearing center"),
                        new Value("222222", "Second hearing center"))
        );
        when(locationRefDataService.getCaseManagementLocationDynamicList()).thenReturn(refDataHearingCentreList);

        changeHearingCentrePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        final DynamicList expectedHearingCentreDynamicList = new DynamicList(
                new Value("111111", "First hearing center"),
                List.of(
                        new Value("111111", "First hearing center"),
                        new Value("222222", "Second hearing center"))
        );

        verify(asylumCase).read(HEARING_CENTRE_DYNAMIC_LIST, DynamicList.class);
        verify(asylumCase).write(HEARING_CENTRE_DYNAMIC_LIST, expectedHearingCentreDynamicList);
    }

    @Test
    void should_not_update_hearing_centre_dynamic_list_when_location_ref_data_is_disabled() {
        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.NO));

        changeHearingCentrePreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verifyNoInteractions(locationRefDataService);
        verify(asylumCase, never()).read(HEARING_CENTRE_DYNAMIC_LIST, DynamicList.class);
        verify(asylumCase, never()).write(eq(HEARING_CENTRE_DYNAMIC_LIST), ArgumentMatchers.any());
    }

}