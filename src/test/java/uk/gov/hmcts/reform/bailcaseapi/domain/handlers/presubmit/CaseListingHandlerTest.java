package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DATE_OF_COMPLIANCE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LISTING_LOCATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.LIST_CASE_HEARING_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.REF_DATA_LISTING_LOCATION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.REF_DATA_LISTING_LOCATION_DETAIL;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_DESCRIPTION;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_LIST;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent.INITIAL_LISTING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent.RELISTING;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingHearingCentre.NEWCASTLE;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.Value;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.DueDateService;
import uk.gov.hmcts.reform.bailcaseapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.clients.model.refdata.CourtVenue;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class CaseListingHandlerTest {

    @Mock private Callback<BailCase> callback;
    @Mock private CaseDetails<BailCase> caseDetails;
    @Mock private BailCase bailCase;
    @Mock private DueDateService dueDateService;
    @Mock private LocationRefDataService locationRefDataService;

    @Captor
    private ArgumentCaptor<BailCaseFieldDefinition> bailExtractorCaptor;
    @Captor
    private ArgumentCaptor<String> bailValueCaptor;

    private CaseListingHandler caseListingHandler;
    private final String caseListHearingDate = "2023-12-01T12:00:00";
    private ZonedDateTime zonedDueDateTime;
    private CourtVenue newCastle;

    @BeforeEach
    public void setUp() {
        caseListingHandler = new CaseListingHandler(dueDateService, locationRefDataService);

        when(callback.getCaseDetails()).thenReturn(caseDetails);
        when(callback.getEvent()).thenReturn(Event.CASE_LISTING);
        when(caseDetails.getCaseData()).thenReturn(bailCase);
        when(bailCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.of(caseListHearingDate));
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(INITIAL_LISTING));

        final ZonedDateTime hearingLocalDate =
            LocalDateTime.parse(caseListHearingDate, ISO_DATE_TIME).toLocalDate().atStartOfDay(ZoneOffset.UTC);
        String dueDate = "2023-11-30";
        zonedDueDateTime = LocalDate.parse(dueDate).atStartOfDay(ZoneOffset.UTC);

        when(dueDateService.calculateHearingDirectionDueDate(hearingLocalDate,
                                                             LocalDate.now()
        )).thenReturn(zonedDueDateTime);

        newCastle = new CourtVenue("Newcastle Civil & Family Courts and Tribunals Centre",
                                   "Newcastle Civil And Family Courts And Tribunals Centre",
                                   "366796",
                                   "Open",
                                   "Y",
                                   "Y",
                                   "Barras Bridge, Newcastle-Upon-Tyne",
                                   "NE1 8QF");
    }

    @Test
    void should_set_case_listing_data() {
        PreSubmitCallbackResponse<BailCase> response = caseListingHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        assertNotNull(response);
        assertEquals(bailCase, response.getData());
        verify(bailCase, times(4)).write(
            bailExtractorCaptor.capture(),
            bailValueCaptor.capture());

        List<BailCaseFieldDefinition> extractors = bailExtractorCaptor.getAllValues();
        List<String> bailCaseValues = bailValueCaptor.getAllValues();

        verify(bailCase, times(1)).write(UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE, YesOrNo.YES);
        assertThat(bailCaseValues.get(extractors.indexOf(SEND_DIRECTION_DESCRIPTION)))
            .containsSequence("You must upload the Bail Summary by the date indicated below.");
        verify(bailCase, times(1)).write(SEND_DIRECTION_LIST, "Home Office");
        verify(bailCase, times(1)).write(DATE_OF_COMPLIANCE,
                                         zonedDueDateTime.toLocalDate().toString());
    }

    @Test
    void should_not_set_case_listing_data() {
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.of(RELISTING));

        PreSubmitCallbackResponse<BailCase> response = caseListingHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        assertNotNull(response);
        assertEquals(bailCase, response.getData());
        verify(bailCase, times(0)).write(
            bailExtractorCaptor.capture(),
            bailValueCaptor.capture());

        verify(bailCase, times(0)).write(UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE, YesOrNo.YES);
        verify(bailCase, times(0)).write(SEND_DIRECTION_LIST, "Home Office");
    }

    @Test
    void should_set_ccd_location_id_and_listing_location_detail_based_on_location_ref_data() {
        when(bailCase.read(IS_BAILS_LOCATION_REFERENCE_DATA_ENABLED, YesOrNo.class)).thenReturn(Optional.of(YesOrNo.YES));
        when(bailCase.read(REF_DATA_LISTING_LOCATION, DynamicList.class)).thenReturn(Optional.of(new DynamicList(
            new Value("366796", "Newcastle"),
            new DynamicList(
                new Value("", ""),
                List.of(new Value("386417", "Hatton Cross"),
                        new Value("366796", "Newcastle")))
                .getListItems()
        )));
        when(locationRefDataService.getCourtVenuesByEpimmsId("366796")).thenReturn(Optional.of(newCastle));

        PreSubmitCallbackResponse<BailCase> response = caseListingHandler.handle(
            PreSubmitCallbackStage.ABOUT_TO_SUBMIT,
            callback
        );

        assertNotNull(response);
        assertEquals(bailCase, response.getData());
        verify(bailCase, times(1)).write(LISTING_LOCATION, NEWCASTLE);
        verify(bailCase, times(1)).write(REF_DATA_LISTING_LOCATION_DETAIL, newCastle);
    }

    @Test
    void should_throw_for_empty_list_case_event() {
        when(bailCase.read(LISTING_EVENT, ListingEvent.class)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> caseListingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("listingEvent is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {

        assertThatThrownBy(() -> caseListingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_START, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(Event.SUBMIT_APPLICATION);
        assertThatThrownBy(() -> caseListingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("Cannot handle callback")
            .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void it_can_handle_callback() {

        for (Event event : Event.values()) {

            when(callback.getEvent()).thenReturn(event);

            for (PreSubmitCallbackStage callbackStage : PreSubmitCallbackStage.values()) {

                boolean canHandle = caseListingHandler.canHandle(callbackStage, callback);

                assertThat(canHandle).isEqualTo(
                    callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && event.equals(Event.CASE_LISTING)
                );
            }

            reset(callback);
        }
    }

    @Test
    void should_throw_for_empty_list_case_hearing_date() {
        when(bailCase.read(LIST_CASE_HEARING_DATE, String.class)).thenReturn(Optional.empty());

        assertThatThrownBy(
            () -> caseListingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, callback))
            .hasMessage("listingHearingDate is not present")
            .isExactlyInstanceOf(RequiredFieldMissingException.class);

    }

    @Test
    void should_not_allow_null_arguments() {

        assertThatThrownBy(() -> caseListingHandler.canHandle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseListingHandler.canHandle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseListingHandler.handle(null, callback))
            .hasMessage("callbackStage must not be null")
            .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> caseListingHandler.handle(PreSubmitCallbackStage.ABOUT_TO_SUBMIT, null))
            .hasMessage("callback must not be null")
            .isExactlyInstanceOf(NullPointerException.class);
    }

}
