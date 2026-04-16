package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
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
            && List.of(EDIT_APPEAL, EDIT_APPEAL_AFTER_SUBMIT, UPDATE_DETENTION_LOCATION).contains(event);
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

        Event event = callback.getEvent();

        YesOrNo appellantInDetention = asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class).orElse(NO);

        if (appellantInDetention.equals(YES) && (event.equals(EDIT_APPEAL) || event.equals(UPDATE_DETENTION_LOCATION))) {
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
                log.info("Clearing IRC and Prison details for Other Detention Centre");
                asylumCase.clear(IRC_NAME);
                asylumCase.clear(PRISON_NAME);
                asylumCase.clear(PRISON_NOMS);
            }

            //Clear custodial sentence date else clear bail
            if (asylumCase.read(CUSTODIAL_SENTENCE, YesOrNo.class).orElse(NO).equals(NO)) {
                log.info("Clearing Custodial Sentence date");
                asylumCase.clear(DATE_CUSTODIAL_SENTENCE);
            } else {
                asylumCase.clear(HAS_PENDING_BAIL_APPLICATIONS);
                asylumCase.clear(BAIL_APPLICATION_NUMBER);
            }
        }

        if (appellantInDetention.equals(YES)) {

            if (asylumCase.read(REMOVAL_ORDER_OPTIONS, YesOrNo.class).orElse(NO).equals(NO)) {
                log.info("Clearing Removal Order date");
                asylumCase.clear(REMOVAL_ORDER_DATE);
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
