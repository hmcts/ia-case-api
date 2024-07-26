package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_CASE_USING_LOCATION_REF_DATA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_CENTRE;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class EditCaseListingPreparerTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private LocationRefDataService locationRefDataService;

    private EditCaseListingPreparer editCaseListingPreparer;

    @BeforeEach
    public void setUp() {

        editCaseListingPreparer =
            new EditCaseListingPreparer(locationRefDataService);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.EDIT_CASE_LISTING);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_populate_listing_location_dynamic_list_if_case_enabled_for_location_ref_data() {

        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        DynamicList freshDynamicList = new DynamicList(
            new Value("", ""),
            List.of(
                new Value("111111", "First hearing center"),
                new Value("222222", "Second hearing center"),
                new Value("333333", "Third hearing center"))
        );

        when(locationRefDataService.getHearingLocationsDynamicList()).thenReturn(freshDynamicList);

        editCaseListingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(1)).write(eq(LISTING_LOCATION), eq(freshDynamicList));
    }

    @Test
    void should_populate_listing_location_with_preselected_dynamic_list_if_case_enabled_for_location_ref_data() {

        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        DynamicList freshDynamicList = new DynamicList(
            new Value("", ""),
            List.of(
                new Value("111111", "First hearing center"),
                new Value("222222", "Second hearing center"),
                new Value("333333", "Third hearing center"),
                new Value("444444", "Fourth newly added hearing center"))
        );

        DynamicList existingDynamicList = new DynamicList(
            new Value("111111", "First hearing center"),
            List.of(
                new Value("111111", "First hearing center"),
                new Value("222222", "Second hearing center"),
                new Value("333333", "Third hearing center"))
        );

        when(locationRefDataService.getHearingLocationsDynamicList()).thenReturn(freshDynamicList);
        when(asylumCase.read(LISTING_LOCATION, DynamicList.class)).thenReturn(Optional.of(existingDynamicList));

        editCaseListingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        DynamicList expectedListingLocation = new DynamicList(
            new Value("111111", "First hearing center"),
            List.of(
                new Value("111111", "First hearing center"),
                new Value("222222", "Second hearing center"),
                new Value("333333", "Third hearing center"),
                new Value("444444", "Fourth newly added hearing center"))
        );

        verify(asylumCase, times(1)).write(eq(LISTING_LOCATION), eq(expectedListingLocation));
    }

    @Test
    void should_populate_fresh_listing_location_without_preselected_dynamic_list_if_case_enabled_for_loc_ref_data() {

        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        DynamicList freshDynamicList = new DynamicList(
            new Value("", ""),
            List.of(
                new Value("111111", "First hearing center"),
                new Value("222222", "Second hearing center"),
                new Value("333333", "Third hearing center"),
                new Value("444444", "Fourth hearing center"))
        );

        DynamicList existingDynamicList = new DynamicList(
            new Value("999999", "Deprecated hearing center"),
            List.of(
                new Value("111111", "First hearing center"),
                new Value("222222", "Second hearing center"),
                new Value("999999", "Deprecated hearing center"))
        );

        when(locationRefDataService.getHearingLocationsDynamicList()).thenReturn(freshDynamicList);
        when(asylumCase.read(LISTING_LOCATION, DynamicList.class)).thenReturn(Optional.of(existingDynamicList));

        editCaseListingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        verify(asylumCase, times(1)).write(eq(LISTING_LOCATION), eq(freshDynamicList));
    }

    @Test
    void should_populate_listing_location_if_case_enabled_for_location_ref_data_by_reading_listCaseHearingCentre() {

        when(asylumCase.read(IS_CASE_USING_LOCATION_REF_DATA, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));

        DynamicList freshDynamicList = new DynamicList(
            new Value("", ""),
            List.of(
                new Value("111111", "First hearing center"),
                new Value("222222", "Second hearing center"),
                new Value("386417", "hattonCross"))
        );

        when(locationRefDataService.getHearingLocationsDynamicList()).thenReturn(freshDynamicList);
        when(asylumCase.read(LISTING_LOCATION, DynamicList.class)).thenReturn(Optional.empty());
        when(asylumCase.read(LIST_CASE_HEARING_CENTRE, HearingCentre.class))
            .thenReturn(Optional.of(HearingCentre.HATTON_CROSS));

        editCaseListingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback);

        DynamicList expectedDynamicList = new DynamicList(
            new Value("386417", "hattonCross"),
            List.of(
                new Value("111111", "First hearing center"),
                new Value("222222", "Second hearing center"),
                new Value("386417", "hattonCross"))
        );
        verify(asylumCase, times(1)).write(eq(LISTING_LOCATION), eq(expectedDynamicList));
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> editCaseListingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SEND_DIRECTION);
        assertThatThrownBy(() -> editCaseListingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = editCaseListingPreparer.canHandle(callbackStage, callback);

                if (event == Event.EDIT_CASE_LISTING
                    && callbackStage == PreSubmitCallbackStage.ABOUT_TO_START) {

                    assertTrue(canHandle);
                } else {
                    assertFalse(canHandle);
                }
            }

            reset(callback);
        }
    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> editCaseListingPreparer.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editCaseListingPreparer.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editCaseListingPreparer.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> editCaseListingPreparer.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }
}
