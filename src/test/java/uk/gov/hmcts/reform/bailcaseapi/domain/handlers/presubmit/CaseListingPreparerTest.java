package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggleService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.REF_DATA_LISTING_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_START;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class CaseListingPreparerTest {

    @Mock
    private Callback<BailCase> callback;
    @Mock
    private CaseDetails<BailCase> caseDetails;
    @Mock
    private BailCase bailCase;
    @Mock
    private FeatureToggleService featureToggleService;
    @Mock
    private LocationRefDataService locationRefDataService;
    @Captor
    private ArgumentCaptor<DynamicList> dynamicListArgumentCaptor;

    private Value hattonCross = new Value("386417", "Hatton Cross");
    private Value newCastle = new Value("366796", "Newcastle");
    private DynamicList locationRefDataDynamicList;


    private CaseListingPreparer caseListingPreparer;

    @BeforeEach
    public void setUp() {
        caseListingPreparer = new CaseListingPreparer(featureToggleService, locationRefDataService);
        locationRefDataDynamicList = new DynamicList(
            new Value("", ""), List.of(hattonCross, newCastle));

        when(locationRefDataService.getHearingLocationsDynamicList())
            .thenReturn(locationRefDataDynamicList);

        when(featureToggleService.locationRefDataEnabled()).thenReturn(true);
        when(callback.getEvent()).thenReturn(Event.CASE_LISTING);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(bailCase);

    }

    @ParameterizedTest
    @CsvSource({"true", "false"})
    void should_set_bails_location_ref_data_field(boolean featureFlag) {
        when(featureToggleService.locationRefDataEnabled()).thenReturn(featureFlag);

        caseListingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);
        verify(bailCase, times(1)).write(
            IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED,
            featureFlag ? YES : YesOrNo.NO
        );
    }

    @Test
    void should_set_location_ref_data() {
        when(bailCase.read(REF_DATA_LISTING_LOCATION, DynamicList.class)).thenReturn(Optional.empty());

        PreSubmitCallbackResponse<BailCase> callbackResponse = caseListingPreparer.handle(
            PreSubmitCallbackStage.ABOUT_TO_START,
            callback
        );

        assertNotNull(callbackResponse);
        verify(bailCase, times(1)).write(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YES);
        verify(bailCase, times(1))
            .write(eq(REF_DATA_LISTING_LOCATION), dynamicListArgumentCaptor.capture());

        assertEquals(List.of(hattonCross, newCastle), dynamicListArgumentCaptor.getValue().getListItems());

    }

    @Test
    void should_pre_select_location_ref_data_when_reopen_the_case() {
        when(bailCase.read(REF_DATA_LISTING_LOCATION, DynamicList.class)).thenReturn(Optional.of(new DynamicList(
            new Value("366796", "Newcastle"),
            locationRefDataDynamicList.getListItems()
        )));

        PreSubmitCallbackResponse<BailCase> callbackResponse = caseListingPreparer.handle(
            PreSubmitCallbackStage.ABOUT_TO_START,
            callback
        );

        assertNotNull(callbackResponse);
        verify(bailCase, times(1)).write(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YES);
        verify(bailCase, times(1))
            .write(eq(REF_DATA_LISTING_LOCATION), dynamicListArgumentCaptor.capture());

        assertEquals(newCastle, dynamicListArgumentCaptor.getValue().getValue());

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> caseListingPreparer
            .canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseListingPreparer
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);


        assertThatThrownBy(() -> caseListingPreparer
            .handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.START_APPLICATION);
        assertThatThrownBy(() -> caseListingPreparer
            .handle(ABOUT_TO_START, null))
            .isExactlyInstanceOf(NullPointerException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {
            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {
                boolean canHandle = caseListingPreparer.canHandle(callbackStage, callback);

                if (event == Event.CASE_LISTING
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {
                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> caseListingPreparer
            .canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseListingPreparer
            .canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
