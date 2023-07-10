package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CHANNEL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
class HearingChannelsDynamicListUpdaterTest {

    public static final String HEARING_CHANNEL_CATEGORY = "HearingChannel";
    public static final String IS_CHILD_REQUIRED = "N";

    private HearingChannelsDynamicListUpdater hearingChannelsDynamicListUpdater;
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
        hearingChannelsDynamicListUpdater =
                new HearingChannelsDynamicListUpdater(refDataUserService);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @ParameterizedTest
    @EnumSource(value = Event.class, names = {
        "UPDATE_HEARING_ADJUSTMENTS",
        "REVIEW_HEARING_REQUIREMENTS",
        "LIST_CASE_WITHOUT_HEARING_REQUIREMENTS"
    })
    void should_populate_dynamic_list(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(refDataUserService.retrieveCategoryValues(HEARING_CHANNEL_CATEGORY, IS_CHILD_REQUIRED))
                .thenReturn(commonDataResponse);
        List<CategoryValues> hearingChannels = List.of(categoryValues);
        when(refDataUserService.filterCategoryValuesByCategoryIdWithActiveFlag(commonDataResponse, HEARING_CHANNEL_CATEGORY))
                .thenReturn(hearingChannels);
        List<Value> values = List.of(value);
        when(refDataUserService.mapCategoryValuesToDynamicListValues(hearingChannels)).thenReturn(values);

        DynamicList dynamicListOfHearingChannel = new DynamicList(new Value("", ""), values);

        hearingChannelsDynamicListUpdater.handle(MID_EVENT, callback);

        ArgumentCaptor<DynamicList> argumentCaptor = ArgumentCaptor.forClass(DynamicList.class);
        verify(asylumCase, times(1)).write(eq(HEARING_CHANNEL), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).usingRecursiveComparison().isEqualTo(dynamicListOfHearingChannel);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPEAL);
        assertThatThrownBy(() -> hearingChannelsDynamicListUpdater.handle(MID_EVENT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.REVIEW_HEARING_REQUIREMENTS);
        assertThatThrownBy(() -> hearingChannelsDynamicListUpdater.handle(ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = hearingChannelsDynamicListUpdater.canHandle(callbackStage, callback);

                if ((List.of(
                        Event.UPDATE_HEARING_ADJUSTMENTS,
                        Event.REVIEW_HEARING_REQUIREMENTS,
                        Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS).contains(event))
                    && callbackStage == MID_EVENT) {

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

        assertThatThrownBy(() -> hearingChannelsDynamicListUpdater.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(
            () -> hearingChannelsDynamicListUpdater.canHandle(MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingChannelsDynamicListUpdater.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> hearingChannelsDynamicListUpdater.handle(PreSubmitCallbackStage.MID_EVENT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
