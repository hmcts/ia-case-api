package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_OUT_OF_COUNTRY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_HAS_FIXED_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_OUT_OF_COUNTRY_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DATE_CLIENT_LEAVE_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DATE_ENTRY_CLEARANCE_DECISION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_LETTER_RECEIVED_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DEPORTATION_ORDER_OPTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.GWF_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_CORRESPONDENCE_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_SPONSOR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_DECISION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OUT_OF_COUNTRY_DECISION_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OUT_OF_COUNTRY_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_ADDRESS_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_AUTHORISATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_EMAIL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_MOBILE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_PARTY_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfCountryDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Slf4j
@Component
public class AppealOutOfCountryEditAppealHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;

    public AppealOutOfCountryEditAppealHandler(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && (callback.getEvent() == Event.EDIT_APPEAL
                   || callback.getEvent() == Event.EDIT_APPEAL_AFTER_SUBMIT)
            && featureToggler.getValue("out-of-country-feature", false)
            && !HandlerUtils.isAipJourney(callback.getCaseDetails().getCaseData());
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

        Optional<YesOrNo> optionalAppellantInUk = asylumCase.read(APPELLANT_IN_UK, YesOrNo.class);

        if (optionalAppellantInUk.isPresent()) {
            YesOrNo appellantInUk = optionalAppellantInUk.get();

            //Clear all the Out of country fields
            if (appellantInUk.equals(YES)) {
                log.info("Clearing Out Of Country fields for an In Country Appeal.");
                asylumCase.write(APPEAL_OUT_OF_COUNTRY, NO);
                asylumCase.clear(HAS_CORRESPONDENCE_ADDRESS);
                asylumCase.clear(APPELLANT_OUT_OF_COUNTRY_ADDRESS);
                asylumCase.clear(OUT_OF_COUNTRY_DECISION_TYPE);
                clearHumanRightsDecision(asylumCase);
                clearRefusalOfProtection(asylumCase);
                asylumCase.clear(DECISION_LETTER_RECEIVED_DATE);
                asylumCase.clear(HAS_SPONSOR);
                asylumCase.clear(OUT_OF_COUNTRY_MOBILE_NUMBER);
                clearSponsor(asylumCase);
            }

            //Clear the In country fields
            if (appellantInUk.equals(NO)) {
                log.info("Clearing In Country fields for Out Of Country Appeal.");
                asylumCase.write(APPEAL_OUT_OF_COUNTRY, YES);
                asylumCase.clear(APPELLANT_HAS_FIXED_ADDRESS);
                asylumCase.clear(APPELLANT_ADDRESS);
                Optional<YesOrNo> optionalHasSponsor = asylumCase.read(HAS_SPONSOR, YesOrNo.class);
                if (optionalHasSponsor.isPresent() && optionalHasSponsor.get().equals(NO)) {
                    clearSponsor(asylumCase);
                }
                asylumCase.read(SPONSOR_CONTACT_PREFERENCE, ContactPreference.class).ifPresent(
                    contactPreference -> {
                        if (contactPreference.equals(ContactPreference.WANTS_EMAIL)) {
                            asylumCase.clear(SPONSOR_MOBILE_NUMBER);
                        } else {
                            asylumCase.clear(SPONSOR_EMAIL);
                        }
                    }
                );

                clearOutOfCountryDecision(asylumCase);
                asylumCase.clear(HOME_OFFICE_DECISION_DATE);
            }

        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void clearSponsor(AsylumCase asylumCase) {
        asylumCase.clear(SPONSOR_GIVEN_NAMES);
        asylumCase.clear(SPONSOR_FAMILY_NAME);
        asylumCase.clear(SPONSOR_ADDRESS);
        asylumCase.clear(SPONSOR_CONTACT_PREFERENCE);
        asylumCase.clear(SPONSOR_EMAIL);
        asylumCase.clear(SPONSOR_MOBILE_NUMBER);
        asylumCase.clear(SPONSOR_AUTHORISATION);
        asylumCase.clear(SPONSOR_NAME_FOR_DISPLAY);
        asylumCase.clear(SPONSOR_ADDRESS_FOR_DISPLAY);
        asylumCase.clear(SPONSOR_PARTY_ID);
    }

    private void clearOutOfCountryDecision(AsylumCase asylumCase) {
        Optional<OutOfCountryDecisionType> outOfCountryDecisionTypeOptional = asylumCase.read(
            OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.class);
        if (outOfCountryDecisionTypeOptional.isPresent()) {
            OutOfCountryDecisionType outOfCountryDecisionType = outOfCountryDecisionTypeOptional.get();
            switch (outOfCountryDecisionType) {
                case REFUSAL_OF_HUMAN_RIGHTS:
                    clearRefusalOfProtection(asylumCase);
                    asylumCase.clear(DECISION_LETTER_RECEIVED_DATE);
                    asylumCase.clear(HOME_OFFICE_REFERENCE_NUMBER);
                    asylumCase.clear(DEPORTATION_ORDER_OPTIONS);
                    break;
                case REFUSAL_OF_PROTECTION:
                    clearHumanRightsDecision(asylumCase);
                    break;
                case REFUSE_PERMIT:
                    clearRefusalOfProtection(asylumCase);
                    asylumCase.clear(DECISION_LETTER_RECEIVED_DATE);
                    asylumCase.clear(HOME_OFFICE_REFERENCE_NUMBER);
                    asylumCase.clear(DEPORTATION_ORDER_OPTIONS);
                    break;
                case REMOVAL_OF_CLIENT:
                    clearHumanRightsDecision(asylumCase);
                    clearRefusalOfProtection(asylumCase);
                    break;
                default:
                    break;
            }
        }
    }

    private void clearHumanRightsDecision(AsylumCase asylumCase) {
        asylumCase.clear(GWF_REFERENCE_NUMBER);
        asylumCase.clear(DATE_ENTRY_CLEARANCE_DECISION);
    }

    private void clearRefusalOfProtection(AsylumCase asylumCase) {
        asylumCase.clear(DATE_CLIENT_LEAVE_UK);
    }

}
