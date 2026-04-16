package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.EnumSource.Mode.INCLUDE;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DETENTION_ADDRESS_LINES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DETENTION_BUILDING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DETENTION_POSTCODE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IRC_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OTHER_DETENTION_FACILITY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility.IRC;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility.OTHER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MARK_APPEAL_AS_DETAINED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_DIRECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority.LATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DetentionFacilityAddressProvider;

@ExtendWith(MockitoExtension.class)
class DetentionLocationAddressPopulatorTest {

    @Mock
    private Callback<AsylumCase> callback;
    @Mock
    private CaseDetails<AsylumCase> caseDetails;
    @Mock
    private DetentionFacilityAddressProvider detentionFacilityAddressProvider;

    private AsylumCase asylumCase = new AsylumCase();
    private DetentionLocationAddressPopulator addressPopulator;
    private DetentionFacilityAddressProvider.DetentionAddress someValidIrcAddress;
    private DetentionFacilityAddressProvider.DetentionAddress someValidPrisonAddress;
    private AddressUk someAppellantAddress;

    @BeforeEach
    public void setUp() {
        addressPopulator = new DetentionLocationAddressPopulator(detentionFacilityAddressProvider);
        lenient().when(callback.getCaseDetails()).thenReturn(caseDetails);
        lenient().when(caseDetails.getCaseData()).thenReturn(asylumCase);

        someValidIrcAddress = new DetentionFacilityAddressProvider.DetentionAddress(
                "some-irc-building",
                "1 some street, some-town, some-county",
                "ABC 123"
        );

        someValidPrisonAddress = new DetentionFacilityAddressProvider.DetentionAddress(
                "some-prison-building",
                "1 some street, some-town, some-county",
                "XYZ 789"
        );

        someAppellantAddress = new AddressUk(
                "some-building",
                "some-street",
                null,
                "some-town",
                "",
                "UVW 321",
                "some-country"
        );
    }

    @ParameterizedTest
    @EnumSource(
          value = Event.class,
          names = {"START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT", "UPDATE_DETENTION_LOCATION"}, mode = INCLUDE)
    void should_populate_detention_location_address_for_irc(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("ircName");
        asylumCase.write(DETENTION_FACILITY, IRC);
        asylumCase.write(IRC_NAME, "some-irc-name");
        asylumCase.write(APPELLANT_IN_DETENTION, YES);

        when(detentionFacilityAddressProvider.getAddressFor("some-irc-name"))
                .thenReturn(Optional.of(someValidIrcAddress));

        PreSubmitCallbackResponse<AsylumCase> response = addressPopulator.handle(MID_EVENT, callback);

        AsylumCase data = response.getData();

        assertThat(data.read(DETENTION_BUILDING)).isEqualTo(Optional.of("some-irc-building"));
        assertThat(data.read(DETENTION_ADDRESS_LINES)).isEqualTo(Optional.of("1 some street, some-town, some-county"));
        assertThat(data.read(DETENTION_POSTCODE)).isEqualTo(Optional.of("ABC 123"));
    }

    @ParameterizedTest
    @EnumSource(
          value = Event.class,
          names = {"START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT", "UPDATE_DETENTION_LOCATION"}, mode = INCLUDE)
    void should_populate_detention_location_address_for_prison(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("prisonName");
        asylumCase.write(DETENTION_FACILITY, IRC);
        asylumCase.write(IRC_NAME, "some-prison-name");
        asylumCase.write(APPELLANT_IN_DETENTION, YES);

        when(detentionFacilityAddressProvider.getAddressFor("some-prison-name"))
                .thenReturn(Optional.of(someValidPrisonAddress));

        PreSubmitCallbackResponse<AsylumCase> response = addressPopulator.handle(MID_EVENT, callback);

        AsylumCase data = response.getData();

        assertThat(data.read(DETENTION_BUILDING)).isEqualTo(Optional.of("some-prison-building"));
        assertThat(data.read(DETENTION_ADDRESS_LINES)).isEqualTo(Optional.of("1 some street, some-town, some-county"));
        assertThat(data.read(DETENTION_POSTCODE)).isEqualTo(Optional.of("XYZ 789"));
    }

    @ParameterizedTest
    @EnumSource(
          value = Event.class,
          names = {"START_APPEAL", "EDIT_APPEAL", "EDIT_APPEAL_AFTER_SUBMIT", "UPDATE_DETENTION_LOCATION"}, mode = INCLUDE)
    void should_populate_detention_location_address_for_other_detention_facility(Event event) {
        when(callback.getEvent()).thenReturn(event);
        when(callback.getPageId()).thenReturn("appellantAddress");
        asylumCase.write(DETENTION_FACILITY, OTHER);
        asylumCase.write(APPELLANT_ADDRESS, someAppellantAddress);
        asylumCase.write(OTHER_DETENTION_FACILITY_NAME, "some-other-facility-name");
        asylumCase.write(APPELLANT_IN_DETENTION, YES);

        PreSubmitCallbackResponse<AsylumCase> response = addressPopulator.handle(MID_EVENT, callback);

        AsylumCase data = response.getData();

        assertThat(data.read(DETENTION_BUILDING)).isEqualTo(Optional.of("some-building"));
        assertThat(data.read(DETENTION_ADDRESS_LINES)).isEqualTo(Optional.of("some-street, some-town, some-country"));
        assertThat(data.read(DETENTION_POSTCODE)).isEqualTo(Optional.of("UVW 321"));
    }

    @Test
    void should_populate_detention_location_address_for_irc_when_marked_as_detained() {
        when(callback.getEvent()).thenReturn(MARK_APPEAL_AS_DETAINED);
        when(callback.getPageId()).thenReturn("markAppealAsDetained_ircName");
        asylumCase.write(DETENTION_FACILITY, IRC);
        asylumCase.write(IRC_NAME, "some-irc-name");
        asylumCase.write(APPELLANT_IN_DETENTION, NO);

        when(detentionFacilityAddressProvider.getAddressFor("some-irc-name"))
              .thenReturn(Optional.of(someValidIrcAddress));

        PreSubmitCallbackResponse<AsylumCase> response = addressPopulator.handle(MID_EVENT, callback);

        AsylumCase data = response.getData();

        assertThat(data.read(DETENTION_BUILDING)).isEqualTo(Optional.of("some-irc-building"));
        assertThat(data.read(DETENTION_ADDRESS_LINES)).isEqualTo(Optional.of("1 some street, some-town, some-county"));
        assertThat(data.read(DETENTION_POSTCODE)).isEqualTo(Optional.of("ABC 123"));
    }

    @Test
    void should_populate_detention_location_address_for_prison_when_marked_as_detained() {
        when(callback.getEvent()).thenReturn(MARK_APPEAL_AS_DETAINED);
        when(callback.getPageId()).thenReturn("markAppealAsDetained_prisonName");
        asylumCase.write(DETENTION_FACILITY, IRC);
        asylumCase.write(IRC_NAME, "some-prison-name");
        asylumCase.write(APPELLANT_IN_DETENTION, NO);

        when(detentionFacilityAddressProvider.getAddressFor("some-prison-name"))
              .thenReturn(Optional.of(someValidPrisonAddress));

        PreSubmitCallbackResponse<AsylumCase> response = addressPopulator.handle(MID_EVENT, callback);

        AsylumCase data = response.getData();

        assertThat(data.read(DETENTION_BUILDING)).isEqualTo(Optional.of("some-prison-building"));
        assertThat(data.read(DETENTION_ADDRESS_LINES)).isEqualTo(Optional.of("1 some street, some-town, some-county"));
        assertThat(data.read(DETENTION_POSTCODE)).isEqualTo(Optional.of("XYZ 789"));
    }

    @Test
    void should_populate_detention_location_address_for_other_detention_facility_when_marked_as_detained() {
        when(callback.getEvent()).thenReturn(MARK_APPEAL_AS_DETAINED);
        when(callback.getPageId()).thenReturn("markAppealAsDetained_updateAppellantAddress");
        asylumCase.write(DETENTION_FACILITY, OTHER);
        asylumCase.write(APPELLANT_ADDRESS, someAppellantAddress);
        asylumCase.write(OTHER_DETENTION_FACILITY_NAME, "some-other-facility-name");
        asylumCase.write(APPELLANT_IN_DETENTION, NO);

        PreSubmitCallbackResponse<AsylumCase> response = addressPopulator.handle(MID_EVENT, callback);

        AsylumCase data = response.getData();

        assertThat(data.read(DETENTION_BUILDING)).isEqualTo(Optional.of("some-building"));
        assertThat(data.read(DETENTION_ADDRESS_LINES)).isEqualTo(Optional.of("some-street, some-town, some-country"));
        assertThat(data.read(DETENTION_POSTCODE)).isEqualTo(Optional.of("UVW 321"));
    }

    @Test
    void should_return_earliest() {
        assertThat(addressPopulator.getDispatchPriority())
                .isEqualTo(LATE);
    }

    @Test
    void handling_should_throw_if_cannot_actually_handle() {
        when(callback.getEvent()).thenReturn(EDIT_APPEAL);
        assertThatThrownBy(() -> addressPopulator.handle(ABOUT_TO_SUBMIT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        when(callback.getEvent()).thenReturn(SEND_DIRECTION);
        assertThatThrownBy(() -> addressPopulator.handle(MID_EVENT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);

        asylumCase.write(APPELLANT_IN_DETENTION, NO);
        when(callback.getEvent()).thenReturn(EDIT_APPEAL);
        when(callback.getPageId()).thenReturn("ircName");
        assertThatThrownBy(() -> addressPopulator.handle(MID_EVENT, callback))
                .hasMessage("Cannot handle callback")
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void should_not_allow_null_arguments() {
        assertThatThrownBy(() -> addressPopulator.canHandle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addressPopulator.canHandle(MID_EVENT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addressPopulator.handle(null, callback))
                .hasMessage("callbackStage must not be null")
                .isExactlyInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> addressPopulator.handle(MID_EVENT, null))
                .hasMessage("callback must not be null")
                .isExactlyInstanceOf(NullPointerException.class);
    }

}