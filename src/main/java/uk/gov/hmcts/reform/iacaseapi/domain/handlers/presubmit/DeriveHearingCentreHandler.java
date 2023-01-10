package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AA_APPELLANT_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AA_APPELLANT_HAS_FIXED_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AGE_ASSESSMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_HAS_FIXED_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_MANAGEMENT_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_SPONSOR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_CENTRE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LEGAL_REP_COMPANY_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.STAFF_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.WA_DUMMY_POSTCODE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre.HARMONDSWORTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingCentre;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.HearingCentreFinder;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.utils.StaffLocation;

@Component
public class DeriveHearingCentreHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final HearingCentreFinder hearingCentreFinder;
    private final CaseManagementLocationService caseManagementLocationService;

    public DeriveHearingCentreHandler(
        HearingCentreFinder hearingCentreFinder,
        CaseManagementLocationService caseManagementLocationService) {
        this.hearingCentreFinder = hearingCentreFinder;
        this.caseManagementLocationService = caseManagementLocationService;
    }

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

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        if (asylumCase.read(HEARING_CENTRE).isEmpty()
            || Event.EDIT_APPEAL_AFTER_SUBMIT.equals(callback.getEvent())) {

            //assign dedicated Hearing Centre Harmondsworth for ADA case
            if (asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.YES) {
                asylumCase.write(HEARING_CENTRE, HARMONDSWORTH);
                asylumCase.write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, HARMONDSWORTH);

                String staffLocationName = StaffLocation.getLocation(HARMONDSWORTH).getName();
                asylumCase.write(STAFF_LOCATION, staffLocationName);
                asylumCase.write(CASE_MANAGEMENT_LOCATION,
                    caseManagementLocationService.getCaseManagementLocation(staffLocationName));
            } else {
                trySetHearingCentreFromPostcode(asylumCase);
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private Optional<String> getAppealPostcode(
        AsylumCase asylumCase
    ) {
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

        boolean isAgeAssessmentAppeal = asylumCase.read(AGE_ASSESSMENT, YesOrNo.class).orElse(NO) == YES;
        AsylumCaseFieldDefinition appellantHasFixedAddress = isAgeAssessmentAppeal ? AA_APPELLANT_HAS_FIXED_ADDRESS : APPELLANT_HAS_FIXED_ADDRESS;
        AsylumCaseFieldDefinition appellantAddressField = isAgeAssessmentAppeal ? AA_APPELLANT_ADDRESS : APPELLANT_ADDRESS;

        if (asylumCase.read(appellantHasFixedAddress, YesOrNo.class)
                .orElse(NO) == YES) {

            Optional<AddressUk> optionalAppellantAddress = asylumCase.read(appellantAddressField);

            if (optionalAppellantAddress.isPresent()) {
                AddressUk appellantAddress = optionalAppellantAddress.get();
                return appellantAddress.getPostCode();
            }
        }

        Optional<AddressUk> optionalLegalRepCompanyAddress =
            asylumCase.read(LEGAL_REP_COMPANY_ADDRESS, AddressUk.class);
        if (optionalLegalRepCompanyAddress.isPresent()) {
            return optionalLegalRepCompanyAddress.get().getPostCode();
        }

        return Optional.empty();
    }

    private void trySetHearingCentreFromPostcode(
        AsylumCase asylumCase
    ) {
        Optional<String> optionalAppellantPostcode = getAppealPostcode(asylumCase);

        if (optionalAppellantPostcode.isPresent()) {
            String appellantPostcode = optionalAppellantPostcode.get();
            HearingCentre hearingCentre = hearingCentreFinder.find(appellantPostcode);
            asylumCase.write(HEARING_CENTRE, hearingCentre);
            asylumCase.write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentre);

            String staffLocationName = StaffLocation.getLocation(hearingCentre).getName();
            asylumCase.write(STAFF_LOCATION, staffLocationName);
            asylumCase.write(CASE_MANAGEMENT_LOCATION,
                caseManagementLocationService.getCaseManagementLocation(staffLocationName));
        } else {
            asylumCase.write(HEARING_CENTRE, hearingCentreFinder.getDefaultHearingCentre());
            asylumCase
                .write(APPLICATION_CHANGE_DESIGNATED_HEARING_CENTRE, hearingCentreFinder.getDefaultHearingCentre());

            String staffLocationName =
                StaffLocation.getLocation(hearingCentreFinder.getDefaultHearingCentre()).getName();
            asylumCase.write(STAFF_LOCATION, staffLocationName);
            asylumCase.write(CASE_MANAGEMENT_LOCATION,
                caseManagementLocationService.getCaseManagementLocation(staffLocationName));
        }
    }

}
