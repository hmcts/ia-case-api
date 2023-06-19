package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
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

/**
 * This handler ensures stale data is removed
 * when case data is written to persistent storage.
 */
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
            && (callback.getEvent() == Event.START_APPEAL || callback.getEvent() == Event.EDIT_APPEAL
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
        Optional<YesOrNo> isAcceleratedDetainedAppeal = asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class);

        if (optionalAppellantInUk.isPresent()) {
            YesOrNo appellantInUk = optionalAppellantInUk.get();

            //Clear all the Out of country fields
            if (appellantInUk.equals(YES)) {
                log.info("Clearing Out Of Country fields for an In Country Appeal.");
                asylumCase.write(APPEAL_OUT_OF_COUNTRY, NO);
                asylumCase.clear(HAS_CORRESPONDENCE_ADDRESS);
                asylumCase.clear(APPELLANT_OUT_OF_COUNTRY_ADDRESS);
                asylumCase.write(OUT_OF_COUNTRY_DECISION_TYPE, OutOfCountryDecisionType.UNKNOWN);
                clearHumanRightsDecision(asylumCase);
                clearRefusalOfProtection(asylumCase);

                YesOrNo isDetained = asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class).orElse(NO);
                // If non-accelerated Detained or non Detained - remove Decision Receive date
                if ((isAcceleratedDetainedAppeal.isPresent() && isAcceleratedDetainedAppeal.equals(Optional.of(NO))) || isDetained.equals(NO)) {
                    asylumCase.clear(DECISION_LETTER_RECEIVED_DATE);
                } else {
                    // if Accelerated Detained
                    asylumCase.clear(HOME_OFFICE_DECISION_DATE);
                }

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
                asylumCase.write(APPELLANT_IN_DETENTION, NO);
                asylumCase.write(IS_ACCELERATED_DETAINED_APPEAL, NO);
                asylumCase.clear(DETENTION_FACILITY);
                asylumCase.clear(DETENTION_STATUS);
                asylumCase.clear(CUSTODIAL_SENTENCE);
                asylumCase.clear(IRC_NAME);
                asylumCase.clear(PRISON_NAME);
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

        } else {
            throw new IllegalStateException("Cannot verify if appeal is in UK or out of country");
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
