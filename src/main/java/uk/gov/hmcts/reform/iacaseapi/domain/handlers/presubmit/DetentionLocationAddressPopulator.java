package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DETENTION_ADDRESS_LINES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DETENTION_BUILDING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DETENTION_POSTCODE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IRC_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PRISON_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility.IRC;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility.OTHER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility.PRISON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.EDIT_APPEAL_AFTER_SUBMIT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.MARK_APPEAL_AS_DETAINED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.START_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.UPDATE_DETENTION_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.MID_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DetentionFacilityAddressProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DetentionFacilityAddressProvider.DetentionAddress;

@Component
public class DetentionLocationAddressPopulator implements PreSubmitCallbackHandler<AsylumCase> {

    private static final Set<String> DETENTION_FACILITY_PAGE_IDS = Set.of(
          "ircName", "prisonName", "appellantAddress");
    private static final Set<String> MARKING_APPEAL_AS_DETAINED_PAGE_IDS = Set.of(
          "markAppealAsDetained_ircName", "markAppealAsDetained_prisonName", "markAppealAsDetained_updateAppellantAddress");
    private static final EnumSet<Event> SUPPORTED_EVENTS = EnumSet.of(
            START_APPEAL, EDIT_APPEAL, EDIT_APPEAL_AFTER_SUBMIT, UPDATE_DETENTION_LOCATION
    );

    private final DetentionFacilityAddressProvider addressProvider;

    public DetentionLocationAddressPopulator(DetentionFacilityAddressProvider addressProvider) {
        this.addressProvider = addressProvider;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        YesOrNo appellantInDetention = callback.getCaseDetails()
                .getCaseData().read(APPELLANT_IN_DETENTION, YesOrNo.class).orElse(NO);

        return (callbackStage == MID_EVENT
                && (SUPPORTED_EVENTS.contains(callback.getEvent()))
                && (DETENTION_FACILITY_PAGE_IDS.contains(callback.getPageId()))
                && appellantInDetention.equals(YES)) || markingAppealAsDetained(appellantInDetention, callback, callbackStage);
    }

    private boolean markingAppealAsDetained(
          YesOrNo appellantInDetention, Callback<AsylumCase> callback, PreSubmitCallbackStage callbackStage) {

        return callbackStage == MID_EVENT &&
              callback.getEvent() == MARK_APPEAL_AS_DETAINED &&
              appellantInDetention.equals(NO) &&
              MARKING_APPEAL_AS_DETAINED_PAGE_IDS.contains(callback.getPageId());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback
                .getCaseDetails()
                .getCaseData();

        String facilityType = asylumCase.read(DETENTION_FACILITY, String.class)
                .orElseThrow(() -> new IllegalArgumentException("not a valid Detention Facility"));

        if (facilityType.equals(IRC.getValue()) || facilityType.equals(PRISON.getValue())) {
            AsylumCaseFieldDefinition nameField = facilityType.equals(IRC.getValue()) ? IRC_NAME : PRISON_NAME;

            String facilityName = asylumCase.read(nameField, String.class)
                  .orElseThrow(() -> new RequiredFieldMissingException(nameField.value()));

            DetentionAddress facilityAddress = addressProvider.getAddressFor(facilityName)
                  .orElseThrow(() -> new RuntimeException("Could not find address for facility: " + facilityName));

            asylumCase.write(DETENTION_BUILDING, facilityAddress.building());
            asylumCase.write(DETENTION_ADDRESS_LINES, facilityAddress.addressLines());
            asylumCase.write(DETENTION_POSTCODE, facilityAddress.postcode());
        }

        if (facilityType.equals(OTHER.getValue())) {
            buildDetentionAddressFromAppellantAddress(asylumCase);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void buildDetentionAddressFromAppellantAddress(AsylumCase asylumCase) {
        AddressUk appellantAddress = asylumCase.read(APPELLANT_ADDRESS, AddressUk.class)
                .orElseThrow(() -> new RequiredFieldMissingException("appellantAddress"));

        asylumCase.write(DETENTION_BUILDING, appellantAddress.getAddressLine1().orElse(""));
        asylumCase.write(DETENTION_ADDRESS_LINES, buildAddressLines(appellantAddress));
        asylumCase.write(DETENTION_POSTCODE, appellantAddress.getPostCode().orElse(""));
    }

    private String buildAddressLines(AddressUk appellantAddress) {
        List<String> addressParts = Stream.of(
                        appellantAddress.getAddressLine2(),
                        appellantAddress.getAddressLine3(),
                        appellantAddress.getPostTown(),
                        appellantAddress.getCounty(),
                        appellantAddress.getCountry()
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(str -> !Strings.isBlank(str))
                .collect(toList());

        return String.join(", ", addressParts);
    }
}
