package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL_IN_ADJUSTMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
import uk.gov.hmcts.reform.iacaseapi.domain.service.RefDataUserService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CategoryValues;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.dto.hearingdetails.CommonDataResponse;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ReviewUpdateHearingRequirementsMidEventHandlerTest {

    public static final String HEARING_CHANNEL = "HearingChannel";
    public static final String IS_CHILD_REQUIRED = "N";

    private ReviewUpdateHearingRequirementsMidEventHandler reviewUpdateHearingRequirementsMidEventHandler;
    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private AsylumCase asylumCase;
    @Mock
    private RefDataUserService refDataUserService;
    @Mock
    private CommonDataResponse commonDataResponse;
    @Mock
    private CategoryValues categoryValues;
    @Mock
    private Value value;

    @BeforeEach
    public void setUp() {
        reviewUpdateHearingRequirementsMidEventHandler =
                new ReviewUpdateHearingRequirementsMidEventHandler(refDataUserService);

        when(callback.getEvent()).thenReturn(Event.UPDATE_HEARING_ADJUSTMENTS);
        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_populate_dynamic_list() {
        List<CategoryValues> hearingChannels = List.of(categoryValues);
        List<Value> values = List.of(value);
        when(refDataUserService.retrieveCategoryValues(HEARING_CHANNEL, IS_CHILD_REQUIRED))
                .thenReturn(commonDataResponse);
        when(refDataUserService.filterCategoryValuesByCategoryIdWithActiveFlag(commonDataResponse, HEARING_CHANNEL))
                .thenReturn(hearingChannels);
        when(refDataUserService.mapCategoryValuesToDynamicListValues(hearingChannels)).thenReturn(values);

        DynamicList dynamicListOfHearingChannel = new DynamicList(new Value("", ""), values);

        reviewUpdateHearingRequirementsMidEventHandler.handle(MID_EVENT, callback);

        ArgumentCaptor<DynamicList> argumentCaptor = ArgumentCaptor.forClass(DynamicList.class);
        verify(asylumCase, times(1)).write(eq(HEARING_CHANNEL_IN_ADJUSTMENT), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(dynamicListOfHearingChannel);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        assertThatThrownBy(() -> reviewUpdateHearingRequirementsMidEventHandler.handle(ABOUT_TO_START, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        assertThatThrownBy(() -> reviewUpdateHearingRequirementsMidEventHandler.handle(ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = reviewUpdateHearingRequirementsMidEventHandler.canHandle(callbackStage, callback);

                if ((callback.getEvent() == Event.UPDATE_HEARING_ADJUSTMENTS)
                    && callbackStage == PreSubmitCallbackStage.MID_EVENT) {

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

        assertThatThrownBy(() -> reviewUpdateHearingRequirementsMidEventHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> reviewUpdateHearingRequirementsMidEventHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewUpdateHearingRequirementsMidEventHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> reviewUpdateHearingRequirementsMidEventHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
