package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

@Component
public class StartAppealMidEvent implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String OUT_OF_COUNTRY_PAGE_ID = "outOfCountry";
    private static final String DETENTION_FACILITY_PAGE_ID = "detentionFacility";
    private static final String SUITABILITY_ATTENDANCE_PAGE_ID = "suitabilityAppellantAttendance";
    private static final String UPPER_TRIBUNAL_REFERENCE_NUMBER_PAGE_ID = "utReferenceNumber";
    private static final String APPELLANTS_ADDRESS_PAGE_ID = "appellantAddress";
    private static final String APPELLANTS_CONTACT_PREFERENCE_PAGE_ID = "appellantContactPreference";
    protected static final String APPELLANTS_ADDRESS_ADMIN_J_PAGE_ID = "appellantAddressAdminJ";
    private static final String LEGAL_REPRESENTATIVE_DETAILS = "legalRepresentativeDetails";
    private static final Pattern UPPER_TRIBUNAL_REFERENCE_NUMBER_PATTERN = Pattern.compile("^UI-[0-9]{4}-[0-9]{6}$");
    private static final Set<String> SUPPORTED_PAGE_IDS = Set.of(
        OUT_OF_COUNTRY_PAGE_ID,
        DETENTION_FACILITY_PAGE_ID,
        SUITABILITY_ATTENDANCE_PAGE_ID,
        UPPER_TRIBUNAL_REFERENCE_NUMBER_PAGE_ID,
        APPELLANTS_ADDRESS_PAGE_ID,
        APPELLANTS_ADDRESS_ADMIN_J_PAGE_ID,
        APPELLANTS_CONTACT_PREFERENCE_PAGE_ID,
        LEGAL_REPRESENTATIVE_DETAILS
    );

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        if (callback.getPageId() == null || callback.getPageId().isBlank()) {
            return false;
        }

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
            && Set.of(
            Event.START_APPEAL,
            Event.EDIT_APPEAL,
            Event.EDIT_APPEAL_AFTER_SUBMIT,
            Event.UPDATE_DETENTION_LOCATION
        ).contains(callback.getEvent())
            && SUPPORTED_PAGE_IDS.contains(callback.getPageId());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        YesOrNo isAdmin = asylumCase.read(IS_ADMIN, YesOrNo.class).orElse(YesOrNo.NO);
        YesOrNo appellantInUk = asylumCase.read(APPELLANT_IN_UK, YesOrNo.class).orElse(YesOrNo.NO);

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
        String pageId = callback.getPageId();
        Event callbackEvent = callback.getEvent();
        if (pageId.equals(DETENTION_FACILITY_PAGE_ID)
            && List.of(Event.START_APPEAL, Event.EDIT_APPEAL).contains(callbackEvent)
            && !asylumCase.read(DETENTION_FACILITY, String.class).orElse("").equals(DetentionFacility.IRC.toString())
        ) {
            asylumCase.write(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.NO);
        }

        if (pageId.equals(OUT_OF_COUNTRY_PAGE_ID)) {
            if (!readAsBool(asylumCase, APPELLANT_IN_UK)) {
                asylumCase.write(APPELLANT_IN_DETENTION, YesOrNo.NO);
            }
            if (callbackEvent == Event.EDIT_APPEAL_AFTER_SUBMIT) {
                // Will only happen for LR as Admins are not allowed to make a OOC appeal as of now.
                CaseDetails<AsylumCase> previousCaseDetails = callback.getCaseDetailsBefore().orElseThrow(() -> new RequiredFieldMissingException("Previous Case Details not found"));
                AsylumCase previousCaseData = previousCaseDetails.getCaseData();
                if (previousCaseData.read(APPELLANT_IN_UK, YesOrNo.class).equals(Optional.of(YesOrNo.NO)) && appellantInUk.equals(YesOrNo.YES)) {
                    // Clear out the OOC data
                    asylumCase.clear(OUT_OF_COUNTRY_DECISION_TYPE);
                    asylumCase.clear(GWF_REFERENCE_NUMBER);
                    asylumCase.clear(DATE_ENTRY_CLEARANCE_DECISION);
                    asylumCase.clear(DATE_CLIENT_LEAVE_UK);
                    clearSponsorDetails(asylumCase);
                }
            }
            if (isAdmin.equals(YesOrNo.YES) && appellantInUk.equals(YesOrNo.NO)) {
                response.addError("This option is currently unavailable");
            }
        } else if (callbackEvent == Event.UPDATE_DETENTION_LOCATION && pageId.equals(DETENTION_FACILITY_PAGE_ID)) {
            boolean isAda = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES);
            String detentionFacilityValue = asylumCase.read(AsylumCaseFieldDefinition.DETENTION_FACILITY, String.class).orElse("");
            if (isAda && !detentionFacilityValue.equals("immigrationRemovalCentre")) {
                response.addError("You cannot update the detention location to a " + detentionFacilityValue + " because this is an accelerated detained appeal.");
            }
        }

        if (pageId.equals(APPELLANTS_CONTACT_PREFERENCE_PAGE_ID)) {
            Optional<String> email = asylumCase.read(EMAIL, String.class);
            Optional<String> emailRetype = asylumCase.read(EMAIL_RETYPE, String.class);

            Optional<String> mobileNumber = asylumCase.read(MOBILE_NUMBER, String.class);
            Optional<String> mobileNumberRetype = asylumCase.read(MOBILE_NUMBER_RETYPE, String.class);

            boolean emailMismatch =
                email.isPresent()
                    && emailRetype.isPresent()
                    && !email.get().equals(emailRetype.get());

            boolean mobileMismatch =
                mobileNumber.isPresent()
                    && mobileNumberRetype.isPresent()
                    && !mobileNumber.get().equals(mobileNumberRetype.get());

            if (emailMismatch || mobileMismatch) {
                response.addError("The details given do not match");
            }
        }

        if (pageId.equals(LEGAL_REPRESENTATIVE_DETAILS)) {

            Optional<String> appellantsMobileNumber =
                asylumCase.read(MOBILE_NUMBER, String.class);

            Optional<String> legalRepMobileNumber =
                asylumCase.read(LEGAL_REP_MOBILE_PHONE_NUMBER, String.class);

            if (appellantsMobileNumber.isPresent()
                && legalRepMobileNumber.isPresent()
                && appellantsMobileNumber.get().equals(legalRepMobileNumber.get())) {

                response.addError(
                    "Contact number is already in use for the appellant. Please amend the appellant's mobile phone number before proceeding."
                );
            }
        }

        boolean isEditOrStartAppealEvent = Set.of(Event.EDIT_APPEAL, Event.EDIT_APPEAL_AFTER_SUBMIT, Event.START_APPEAL).contains(callbackEvent);
        if (pageId.equals(SUITABILITY_ATTENDANCE_PAGE_ID) && isEditOrStartAppealEvent) {
            boolean suitabilityHearingType = asylumCase.read(SUITABILITY_HEARING_TYPE_YES_OR_NO, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES);

            if (suitabilityHearingType) {
                asylumCase.write(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_2, YesOrNo.NO);
            } else {
                asylumCase.write(SUITABILITY_APPELLANT_ATTENDANCE_YES_OR_NO_1, YesOrNo.NO);
            }

        }

        if (pageId.equals(UPPER_TRIBUNAL_REFERENCE_NUMBER_PAGE_ID) && isEditOrStartAppealEvent) {
            String upperTribunalReferenceNumber = asylumCase
                .read(UPPER_TRIBUNAL_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new IllegalStateException("upperTribunalReferenceNumber is missing"));

            if (!UPPER_TRIBUNAL_REFERENCE_NUMBER_PATTERN.matcher(upperTribunalReferenceNumber).matches()) {
                response.addError("Enter the Upper Tribunal reference number in the format UI-Year of submission-6 digit number. For example, UI-2020-123456.");
            }
        }

        if (isAdmin.equals(YesOrNo.YES)
            && (pageId.equals(APPELLANTS_ADDRESS_PAGE_ID) || pageId.equals(APPELLANTS_ADDRESS_ADMIN_J_PAGE_ID))
            && isEditOrStartAppealEvent
            && (asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS, YesOrNo.class).equals(Optional.of(YesOrNo.NO))
            || asylumCase.read(APPELLANT_HAS_FIXED_ADDRESS_ADMIN_J, YesOrNo.class).equals(Optional.of(YesOrNo.NO)))) {
            response.addError("The appellant must have provided a postal address");
        }

        // DIAC-1413 bugfix to show bail page for IRC, having returned in the journey, after originally selecting
        // CUSTODIAL_SENTENCE yes via prison or other
        if (pageId.equals(DETENTION_FACILITY_PAGE_ID)
            && Set.of(Event.START_APPEAL, Event.EDIT_APPEAL).contains(callbackEvent)) {
            String detentionFacilityValue = asylumCase.read(AsylumCaseFieldDefinition.DETENTION_FACILITY, String.class).orElse("");
            if (detentionFacilityValue.equals("immigrationRemovalCentre")) {
                asylumCase.write(CUSTODIAL_SENTENCE, YesOrNo.NO);
                asylumCase.clear(DATE_CUSTODIAL_SENTENCE);
            }
        }

        return response;
    }

    private boolean readAsBool(AsylumCase data, AsylumCaseFieldDefinition fieldDef) {
        return data.read(fieldDef, YesOrNo.class).orElse(YesOrNo.NO).equals(YesOrNo.YES);
    }

    private void clearSponsorDetails(AsylumCase asylumCase) {
        asylumCase.clear(HAS_SPONSOR);
        asylumCase.clear(SPONSOR_GIVEN_NAMES);
        asylumCase.clear(SPONSOR_FAMILY_NAME);
        asylumCase.clear(SPONSOR_ADDRESS);
        asylumCase.clear(SPONSOR_CONTACT_PREFERENCE);
        asylumCase.clear(SPONSOR_EMAIL);
        asylumCase.clear(SPONSOR_MOBILE_NUMBER);
        asylumCase.clear(SPONSOR_AUTHORISATION);
    }
}
