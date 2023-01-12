package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AGE_ASSESSMENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DATE_ON_DECISION_LETTER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DECISION_LETTER_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HSC_TRUST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LITIGATION_FRIEND;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LITIGATION_FRIEND_COMPANY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LITIGATION_FRIEND_CONTACT_PREFERENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LITIGATION_FRIEND_EMAIL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LITIGATION_FRIEND_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LITIGATION_FRIEND_GIVEN_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LITIGATION_FRIEND_PHONE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LOCAL_AUTHORITY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ORGANISATION_ON_DECISION_LETTER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OrganisationOnDecisionLetter;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
public class AgeAssessmentDataEditAppealHandler implements PreSubmitCallbackHandler<AsylumCase> {
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.EDIT_APPEAL);
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

        Optional<YesOrNo> isAgeAssessmentAppeal = asylumCase.read(AGE_ASSESSMENT, YesOrNo.class);

        if (isAgeAssessmentAppeal.equals(Optional.of(YES))) {
            String organisationOnDecisionLetter = asylumCase.read(ORGANISATION_ON_DECISION_LETTER, String.class)
                    .orElseThrow(() -> new RequiredFieldMissingException("Organisation on decision letter missing"));

            //Clear all 'hscTrust' field
            if (organisationOnDecisionLetter.equals(OrganisationOnDecisionLetter.LOCAL_AUTHORITY.toString())) {
                log.info("Clearing Prison details for an IRC Detention centre.");
                asylumCase.clear(HSC_TRUST);
            }

            //Clear all 'localAuthority' & 'hscTrust' fields
            if (organisationOnDecisionLetter
                    .equals(OrganisationOnDecisionLetter.NATIONAL_AGE_ASSESSMENT_BOARD.toString())) {
                log.info("Clearing IRC details for a Prison Detention Centre");
                asylumCase.clear(LOCAL_AUTHORITY);
                asylumCase.clear(HSC_TRUST);
            }

            //Clear all 'localAuthority' field
            if (organisationOnDecisionLetter.equals(OrganisationOnDecisionLetter.HSC_TRUST.toString())) {
                log.info("Clearing IRC details for a Prison Detention Centre");
                asylumCase.clear(LOCAL_AUTHORITY);
            }

            //Clear litigation friend data
            if (asylumCase.read(LITIGATION_FRIEND, YesOrNo.class).orElse(NO).equals(NO)) {
                clearLitigationFriendData(asylumCase);
            }

            //Clear litigation friend contact preference data
            if (asylumCase.read(LITIGATION_FRIEND, YesOrNo.class).orElse(NO).equals(YES)) {
                log.info("Clearing Litigation Friend Contact Preference");
                asylumCase.read(LITIGATION_FRIEND_CONTACT_PREFERENCE, ContactPreference.class).ifPresent(
                        contactPreference -> {
                            if (contactPreference.equals(ContactPreference.WANTS_EMAIL)) {
                                asylumCase.clear(LITIGATION_FRIEND_PHONE_NUMBER);
                            } else {
                                asylumCase.clear(LITIGATION_FRIEND_EMAIL);
                            }
                        }
                );
            }
        }

        //Clear all age assessment related data
        if (isAgeAssessmentAppeal.equals(Optional.of(NO))) {
            asylumCase.clear(ORGANISATION_ON_DECISION_LETTER);
            asylumCase.clear(LOCAL_AUTHORITY);
            asylumCase.clear(HSC_TRUST);
            asylumCase.clear(DECISION_LETTER_REFERENCE_NUMBER);
            asylumCase.clear(DATE_ON_DECISION_LETTER);
            clearLitigationFriendData(asylumCase);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void clearLitigationFriendData(AsylumCase asylumCase) {
        log.info("Clearing Litigation Friend data");
        asylumCase.clear(LITIGATION_FRIEND_GIVEN_NAME);
        asylumCase.clear(LITIGATION_FRIEND_FAMILY_NAME);
        asylumCase.clear(LITIGATION_FRIEND_COMPANY);
        asylumCase.clear(LITIGATION_FRIEND_CONTACT_PREFERENCE);
        asylumCase.clear(LITIGATION_FRIEND_EMAIL);
        asylumCase.clear(LITIGATION_FRIEND_PHONE_NUMBER);
    }
}
