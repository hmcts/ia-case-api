package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.HARMONDSWORTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_SPONSOR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE_DYNAMIC_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STAFF_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WA_DUMMY_POSTCODE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isCaseUsingLocationRefData;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DynamicList;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.StaffLocation;

@Component
@RequiredArgsConstructor
public class DeriveHearingCentreHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HearingCentreFinder hearingCentreFinder;
    private final CaseManagementLocationService caseManagementLocationService;
    private final LocationRefDataService locationRefDataService;

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && Arrays.asList(
                        Event.SUBMIT_APPEAL,
                        Event.EDIT_APPEAL_AFTER_SUBMIT)
                .contains(callback.getEvent());
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<String> detentionFacility = asylumCase
            .read(DETENTION_FACILITY, String.class);
        boolean appellantInDetention = asylumCase
            .read(APPELLANT_IN_DETENTION, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES;

        if (hearingCentreNotSet(asylumCase)
                || Event.EDIT_APPEAL_AFTER_SUBMIT.equals(callback.getEvent())) {

            if (appellantInDetention) {
                // for detained non-ADA non-AAA cases, set Hearing Centre according to Detention Facility
                if (asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class).orElse(NO) == NO
                    && asylumCase.read(AGE_ASSESSMENT, YesOrNo.class).orElse(NO) == NO
                    && !detentionFacility.equals(Optional.of(DetentionFacility.OTHER.getValue()))) {
                    setHearingCentreFromDetentionFacilityName(asylumCase);
                } else {
                    //assign dedicated Hearing Centre Harmondsworth for all other detained appeals
                    asylumCase.write(HEARING_CENTRE, HARMONDSWORTH);
                    asylumCase.write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, HARMONDSWORTH);

                    String staffLocationName = StaffLocation.getLocation(HARMONDSWORTH).getName();
                    asylumCase.write(STAFF_LOCATION, staffLocationName);
                    asylumCase.write(CASE_MANAGEMENT_LOCATION,
                        caseManagementLocationService.getCaseManagementLocation(staffLocationName));
                }

            } else {
                // set Hearing Centre by Postcode for non-detained cases
                setHearingCentreFromPostcode(asylumCase);
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private Optional<String> getAppealPostcode(AsylumCase asylumCase) {
        Optional<String> optionalWaDummyPostcode = asylumCase.read(WA_DUMMY_POSTCODE, String.class);

        if (optionalWaDummyPostcode.isPresent()) {
            return optionalWaDummyPostcode;
        }
        if (asylumCase.read(HAS_SPONSOR, YesOrNo.class)
                .orElse(NO) == YES) {
            Optional<AddressUk> optionalSponsorAddress = asylumCase.read(SPONSOR_ADDRESS, AddressUk.class);

            if (optionalSponsorAddress.isPresent()) {
                return optionalSponsorAddress.get().getPostCode();
            }
        }

        Optional<AddressUk> optionalAppellantAddress = asylumCase.read(APPELLANT_ADDRESS);

        if (optionalAppellantAddress.isPresent()) {
            AddressUk appellantAddress = optionalAppellantAddress.get();
            return appellantAddress.getPostCode();
        }

        Optional<AddressUk> optionalLegalRepCompanyAddress =
                asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class);
        if (optionalLegalRepCompanyAddress.isPresent()) {
            return optionalLegalRepCompanyAddress.get().getPostCode();
        }

        Optional<AddressUk> optionalEjpLegalPracticeAddress =
            asylumCase.read(LEGAL_PRACTICE_ADDRESS_EJP, AddressUk.class);
        if (optionalEjpLegalPracticeAddress.isPresent()) {
            return optionalEjpLegalPracticeAddress.get().getPostCode();
        }

        return Optional.empty();
    }

    private void setHearingCentreFromPostcode(AsylumCase asylumCase) {
        Optional<String> optionalAppellantPostcode = getAppealPostcode(asylumCase);

        HearingCentre hearingCentre = optionalAppellantPostcode
            .map(hearingCentreFinder::find)
            .orElseGet(hearingCentreFinder::getDefaultHearingCentre);

        if (isCaseUsingLocationRefData(asylumCase)) {
            DynamicList hearingCentreDynamicList = locationRefDataService.getHearingCentreDynamicList();
            hearingCentreDynamicList.getListItems().stream()
                .filter(value -> Objects.equals(value.getCode(), hearingCentre.getEpimsId()))
                .findFirst().ifPresent(hearingCentreDynamicList::setValue);

            asylumCase.write(HEARING_CENTRE_DYNAMIC_LIST, hearingCentreDynamicList);
        }

        asylumCase.write(HEARING_CENTRE, hearingCentre);
        asylumCase.write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentre);

        String staffLocationName = StaffLocation.getLocation(hearingCentre).getName();
        asylumCase.write(STAFF_LOCATION, staffLocationName);
        asylumCase.write(CASE_MANAGEMENT_LOCATION,
            caseManagementLocationService.getCaseManagementLocation(staffLocationName));
    }

    public void setHearingCentreFromDetentionFacilityName(AsylumCase asylumCase){
        final String prisonName = asylumCase.read(PRISON_NAME, String.class).orElse("");

        final String ircName = asylumCase.read(IRC_NAME, String.class).orElse("");

        if (prisonName.isEmpty() && ircName.isEmpty()) {
            throw new RequiredFieldMissingException("Prison name and IRC name missing");

        } else {

            String detentionFacilityName = !prisonName.isEmpty() ? prisonName : ircName;

            HearingCentre hearingCentre = hearingCentreFinder.findByDetentionFacility(detentionFacilityName);
            asylumCase.write(HEARING_CENTRE, hearingCentre);
            asylumCase.write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentre);

            String staffLocationName = StaffLocation.getLocation(hearingCentre).getName();
            asylumCase.write(STAFF_LOCATION, staffLocationName);
            asylumCase.write(CASE_MANAGEMENT_LOCATION,
                caseManagementLocationService.getCaseManagementLocation(staffLocationName));
        }
    }

    private boolean hearingCentreNotSet(AsylumCase asylumCase) {

        return isCaseUsingLocationRefData(asylumCase)
            ? asylumCase.read(HEARING_CENTRE_DYNAMIC_LIST).isEmpty()
            : asylumCase.read(HEARING_CENTRE).isEmpty();
    }
}
