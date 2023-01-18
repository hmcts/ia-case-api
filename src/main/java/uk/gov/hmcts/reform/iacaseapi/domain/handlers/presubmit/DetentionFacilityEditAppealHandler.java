package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CUSTODIAL_SENTENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DATE_CUSTODIAL_SENTENCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IRC_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.OTHER_DETENTION_FACILITY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PRISON_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PRISON_NOMS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMOVAL_ORDER_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REMOVAL_ORDER_OPTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility.IRC;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility.OTHER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.DetentionFacility.PRISON;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
public class DetentionFacilityEditAppealHandler implements PreSubmitCallbackHandler<AsylumCase> {

    public DetentionFacilityEditAppealHandler() {
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        Event event = callback.getEvent();

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && List.of(EDIT_APPEAL, EDIT_APPEAL_AFTER_SUBMIT).contains(event);
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

        YesOrNo appellantInDetention = asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class).orElse(NO);

        if (appellantInDetention.equals(YES)) {
            String facility = asylumCase.read(DETENTION_FACILITY, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("Detention Facility missing"));

            //Clear all 'prison' & 'other' fields
            if (facility.equals(IRC.getValue())) {
                log.info("Clearing Prison details for an IRC Detention centre.");
                asylumCase.clear(PRISON_NAME);
                asylumCase.clear(PRISON_NOMS);
                asylumCase.clear(OTHER_DETENTION_FACILITY_NAME);
                asylumCase.clear(CUSTODIAL_SENTENCE);
            }

            //Clear all 'irc' & 'other' fields
            if (facility.equals(PRISON.getValue())) {
                log.info("Clearing IRC details for a Prison Detention Centre");
                asylumCase.clear(IRC_NAME);
                asylumCase.clear(OTHER_DETENTION_FACILITY_NAME);
            }

            //Clear all 'irc' & 'prison' fields
            if (facility.equals(OTHER.getValue())) {
                log.info("Clearing IRC details for a Prison Detention Centre");
                asylumCase.clear(IRC_NAME);
                asylumCase.clear(PRISON_NAME);
                asylumCase.clear(PRISON_NOMS);
                asylumCase.clear(CUSTODIAL_SENTENCE);
            }

            //Clear custodial sentence date
            if (asylumCase.read(CUSTODIAL_SENTENCE, YesOrNo.class).orElse(NO).equals(NO)) {
                log.info("Clearing Custodial Sentence date");
                asylumCase.clear(DATE_CUSTODIAL_SENTENCE);
            }

            if (asylumCase.read(REMOVAL_ORDER_OPTIONS, YesOrNo.class).orElse(NO).equals(NO)) {
                log.info("Clearing Removal Order date");
                asylumCase.clear(REMOVAL_ORDER_DATE);
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
