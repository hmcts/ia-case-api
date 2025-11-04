package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AIP_SPONSOR_EMAIL_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AIP_SPONSOR_MOBILE_NUMBER_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_OUT_OF_COUNTRY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE_PREVIOUS_SELECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_DATE_OF_BIRTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_HAS_FIXED_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK_PREVIOUS_SELECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NATIONALITIES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NATIONALITIES_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_OUT_OF_COUNTRY_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_STATELESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DATE_CLIENT_LEAVE_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_HEARING_FEE_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_LETTER_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.GWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_SPONSOR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OUTSIDE_UK_WHEN_APPLICATION_MADE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OUTSIDE_UK_WHEN_APPLICATION_MADE_PREVIOUS_SELECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.RP_DC_APPEAL_HEARING_OPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SEARCH_POSTCODE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_ADDRESS_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_AUTHORISATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_EMAIL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_EMAIL_ADMIN_J;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_MOBILE_NUMBER_ADMIN_J;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_SUBSCRIPTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SUBSCRIPTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_THE_NOTICE_OF_DECISION_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_THE_NOTICE_OF_DECISION_EXPLANATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Slf4j
@Component
public class AppealOutOfCountryEditAppealAipHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;

    public AppealOutOfCountryEditAppealAipHandler(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && (callback.getEvent() == Event.EDIT_APPEAL || callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT)
            && featureToggler.getValue("aip-ooc-feature", false)
            && HandlerUtils.isAipJourney(callback.getCaseDetails().getCaseData());
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

        final long caseId = callback.getCaseDetails().getId();
        boolean holdFieldsForInUkChange = true;
        boolean holdFieldsForAppealTypeChange = true;
        boolean holdFieldsForOutsideUkWhenApplicationMadeChange = true;

        Optional<YesOrNo> optionalAppellantInUk =
            asylumCase.read(APPELLANT_IN_UK, YesOrNo.class);

        Optional<AppealType> optionalAppealType =
            asylumCase.read(APPEAL_TYPE, AppealType.class);

        Optional<YesOrNo> optionalOutsideUkWhenApplicationMade =
            asylumCase.read(OUTSIDE_UK_WHEN_APPLICATION_MADE, YesOrNo.class);

        if (optionalAppellantInUk.isPresent()) {
            Optional<YesOrNo> optionalAppellantInUkPreviousSelection = Optional.of(
                asylumCase.read(APPELLANT_IN_UK_PREVIOUS_SELECTION, YesOrNo.class)
                    .orElse(optionalAppellantInUk.get()));

            asylumCase.write(APPELLANT_IN_UK_PREVIOUS_SELECTION, optionalAppellantInUkPreviousSelection);
            holdFieldsForInUkChange = optionalAppellantInUk.get().equals(optionalAppellantInUkPreviousSelection.get());
        }

        if (optionalAppealType.isPresent()) {
            Optional<AppealType> optionalAppealTypePreviousSelection = Optional.of(
                asylumCase.read(APPEAL_TYPE_PREVIOUS_SELECTION, AppealType.class)
                    .orElse(optionalAppealType.get()));

            asylumCase.write(APPEAL_TYPE_PREVIOUS_SELECTION, optionalAppealTypePreviousSelection);
            holdFieldsForAppealTypeChange = optionalAppealType.get().equals(optionalAppealTypePreviousSelection.get());
        }

        if (optionalOutsideUkWhenApplicationMade.isPresent()) {
            Optional<YesOrNo> optionalOutsideUkWhenApplicationMadePreviousSelection = Optional.of(
                asylumCase.read(OUTSIDE_UK_WHEN_APPLICATION_MADE_PREVIOUS_SELECTION, YesOrNo.class)
                    .orElse(optionalOutsideUkWhenApplicationMade.get()));

            asylumCase.write(OUTSIDE_UK_WHEN_APPLICATION_MADE_PREVIOUS_SELECTION, optionalOutsideUkWhenApplicationMadePreviousSelection);
            holdFieldsForOutsideUkWhenApplicationMadeChange = optionalOutsideUkWhenApplicationMade.get().equals(optionalOutsideUkWhenApplicationMadePreviousSelection.get());
        }

        updatePreviousSelections(
            asylumCase,
            holdFieldsForInUkChange,
            holdFieldsForAppealTypeChange,
            holdFieldsForOutsideUkWhenApplicationMadeChange,
            optionalAppellantInUk,
            optionalAppealType,
            optionalOutsideUkWhenApplicationMade
        );

        verifyInUkChange(
            optionalAppellantInUk,
            asylumCase,
            holdFieldsForInUkChange,
            caseId
        );

        Optional<YesOrNo> optionalHasSponsor = asylumCase.read(HAS_SPONSOR, YesOrNo.class);

        if (optionalHasSponsor.isPresent() && optionalHasSponsor.get().equals(NO)) {
            clearSponsor(asylumCase);
        } else {
            writeSponsorContactDetails(asylumCase);
        }

        if (optionalAppealType.isPresent() && !holdFieldsForAppealTypeChange) {
            log.info("Clearing fields for Appeal Type change for AIP case Id [{}]", caseId);
            clearAipFieldsForAppealType(asylumCase, false);
        }

        if (optionalOutsideUkWhenApplicationMade.isPresent() && !holdFieldsForOutsideUkWhenApplicationMadeChange) {
            log.info("Clearing fields for Outside UK When Application Made change for AIP case Id [{}]", caseId);
            clearAipFieldsForOutsideUkWhenApplicationMade(asylumCase);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void updatePreviousSelections(
        AsylumCase asylumCase,
        boolean holdFieldsForInUkChange,
        boolean holdFieldsForAppealTypeChange,
        boolean holdFieldsForOutsideUkWhenApplicationMadeChange,
        Optional<YesOrNo> optionalAppellantInUk,
        Optional<AppealType> optionalAppealType,
        Optional<YesOrNo> optionalOutsideUkWhenApplicationMade
    ) {
        if (!holdFieldsForInUkChange) {
            asylumCase.write(APPELLANT_IN_UK_PREVIOUS_SELECTION, optionalAppellantInUk);
        }
        if (!holdFieldsForAppealTypeChange) {
            asylumCase.write(APPEAL_TYPE_PREVIOUS_SELECTION, optionalAppealType);
        }
        if (!holdFieldsForOutsideUkWhenApplicationMadeChange) {
            asylumCase.write(OUTSIDE_UK_WHEN_APPLICATION_MADE_PREVIOUS_SELECTION, optionalOutsideUkWhenApplicationMade);
        }
    }

    private void verifyInUkChange(
        Optional<YesOrNo> optionalAppellantInUk,
        AsylumCase asylumCase,
        boolean holdFieldsForInUkChange,
        long caseId
    ) {
        if (optionalAppellantInUk.isPresent()) {
            YesOrNo appellantInUk = optionalAppellantInUk.get();

            if (appellantInUk.equals(YES)) {
                asylumCase.write(APPEAL_OUT_OF_COUNTRY, NO);
                asylumCase.clear(HAS_CORRESPONDENCE_ADDRESS);
                if (!holdFieldsForInUkChange) {
                    log.info("Clearing Out Of Country fields for an In Country AIP Appeal with case Id [{}]", caseId);
                    clearAipFieldsForAppealType(asylumCase, true);
                }
            }

            if (appellantInUk.equals(NO)) {
                asylumCase.write(APPEAL_OUT_OF_COUNTRY, YES);
                asylumCase.write(HAS_CORRESPONDENCE_ADDRESS, YES);
                if (!holdFieldsForInUkChange) {
                    log.info("Clearing In Country fields for Out Of Country AIP Appeal with case Id [{}]", caseId);
                    clearAipFieldsForAppealType(asylumCase, true);
                }
            }

            asylumCase.write(HAS_SPONSOR, asylumCase.read(HAS_SPONSOR, YesOrNo.class).orElse(null));
        }
    }

    private void writeSponsorContactDetails(AsylumCase asylumCase) {
        Optional<List<IdValue<Subscriber>>> maybeSponsorSubscriptions =
                asylumCase.read(SPONSOR_SUBSCRIPTIONS);

        final List<IdValue<Subscriber>> existingSponsorSubscriptions =
                maybeSponsorSubscriptions.orElse(Collections.emptyList());

        if (!existingSponsorSubscriptions.isEmpty()) {
            final IdValue<Subscriber> sponsorDetails = existingSponsorSubscriptions.get(0);

            String sponsorEmail = sponsorDetails.getValue().getEmail();
            String sponsorMobile = sponsorDetails.getValue().getMobileNumber();

            if (sponsorEmail != null && sponsorMobile != null) {
                YesOrNo isAdmin = asylumCase.read(IS_ADMIN, YesOrNo.class).orElse(YesOrNo.NO);

                if (isAdmin.equals(YesOrNo.YES)) {
                    asylumCase.write(SPONSOR_EMAIL_ADMIN_J, sponsorEmail);
                    asylumCase.write(SPONSOR_MOBILE_NUMBER_ADMIN_J, sponsorMobile);
                } else {
                    asylumCase.write(SPONSOR_EMAIL, sponsorEmail);
                    asylumCase.write(AIP_SPONSOR_EMAIL_FOR_DISPLAY, sponsorEmail);
                    asylumCase.write(SPONSOR_MOBILE_NUMBER, sponsorMobile);
                    asylumCase.write(AIP_SPONSOR_MOBILE_NUMBER_FOR_DISPLAY, sponsorMobile);
                }
            }
        }
    }

    private void clearAipFieldsForAppealType(AsylumCase asylumCase, boolean clearAppealType) {
        if (clearAppealType) {
            clearAipAppealTypeDetails(asylumCase);
        }
        clearAipHomeOfficeDetails(asylumCase);
        clearAipPersonalDetails(asylumCase);
        clearAipContactDetails(asylumCase);
        clearAipDecisionType(asylumCase);
    }

    private void clearAipFieldsForOutsideUkWhenApplicationMade(AsylumCase asylumCase) {
        asylumCase.clear(HOME_OFFICE_REFERENCE_NUMBER);
        asylumCase.clear(HOME_OFFICE_DECISION_DATE);
        asylumCase.clear(DECISION_LETTER_RECEIVED_DATE);
        asylumCase.clear(UPLOAD_THE_NOTICE_OF_DECISION_DOCS);
        asylumCase.clear(UPLOAD_THE_NOTICE_OF_DECISION_EXPLANATION);
        asylumCase.clear(GWF_REFERENCE_NUMBER);
        asylumCase.clear(DATE_CLIENT_LEAVE_UK);
        clearAipPersonalDetails(asylumCase);
        clearAipContactDetails(asylumCase);
        clearAipDecisionType(asylumCase);
    }

    private void clearAipAppealTypeDetails(AsylumCase asylumCase) {
        asylumCase.clear(APPEAL_TYPE);
    }

    private void clearAipHomeOfficeDetails(AsylumCase asylumCase) {
        asylumCase.clear(HOME_OFFICE_REFERENCE_NUMBER);
        asylumCase.clear(HOME_OFFICE_DECISION_DATE);
        asylumCase.clear(DECISION_LETTER_RECEIVED_DATE);
        asylumCase.clear(UPLOAD_THE_NOTICE_OF_DECISION_DOCS);
        asylumCase.clear(UPLOAD_THE_NOTICE_OF_DECISION_EXPLANATION);
        asylumCase.clear(GWF_REFERENCE_NUMBER);
        asylumCase.clear(DATE_CLIENT_LEAVE_UK);
        asylumCase.clear(OUTSIDE_UK_WHEN_APPLICATION_MADE);
    }

    private void clearAipPersonalDetails(AsylumCase asylumCase) {
        asylumCase.clear(APPELLANT_GIVEN_NAMES);
        asylumCase.clear(APPELLANT_FAMILY_NAME);
        asylumCase.clear(APPELLANT_DATE_OF_BIRTH);
        asylumCase.clear(APPELLANT_NATIONALITIES);
        asylumCase.clear(APPELLANT_NATIONALITIES_DESCRIPTION);
        asylumCase.clear(APPELLANT_STATELESS);
        asylumCase.clear(APPELLANT_HAS_FIXED_ADDRESS);
        asylumCase.clear(APPELLANT_ADDRESS);
        asylumCase.clear(SEARCH_POSTCODE);
        asylumCase.clear(APPELLANT_OUT_OF_COUNTRY_ADDRESS);
    }

    private void clearAipContactDetails(AsylumCase asylumCase) {
        asylumCase.clear(SUBSCRIPTIONS);
        asylumCase.clear(HAS_SPONSOR);
        clearSponsor(asylumCase);
    }

    private void clearSponsor(AsylumCase asylumCase) {
        asylumCase.clear(SPONSOR_GIVEN_NAMES);
        asylumCase.clear(SPONSOR_FAMILY_NAME);
        asylumCase.clear(SPONSOR_ADDRESS);
        asylumCase.clear(SPONSOR_CONTACT_PREFERENCE);
        asylumCase.clear(SPONSOR_SUBSCRIPTIONS);
        asylumCase.clear(SPONSOR_EMAIL);
        asylumCase.clear(AIP_SPONSOR_EMAIL_FOR_DISPLAY);
        asylumCase.clear(SPONSOR_MOBILE_NUMBER);
        asylumCase.clear(AIP_SPONSOR_MOBILE_NUMBER_FOR_DISPLAY);
        asylumCase.clear(SPONSOR_AUTHORISATION);
        asylumCase.clear(SPONSOR_NAME_FOR_DISPLAY);
        asylumCase.clear(SPONSOR_ADDRESS_FOR_DISPLAY);
        asylumCase.clear(SPONSOR_PARTY_ID);
    }

    private void clearAipDecisionType(AsylumCase asylumCase) {
        asylumCase.clear(RP_DC_APPEAL_HEARING_OPTION);
        asylumCase.clear(DECISION_HEARING_FEE_OPTION);
    }
}
