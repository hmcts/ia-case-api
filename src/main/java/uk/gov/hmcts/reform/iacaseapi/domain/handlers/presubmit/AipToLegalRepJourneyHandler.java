package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREV_JOURNEY_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PRE_CLARIFYING_STATE;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ContactPreference;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Subscriber;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackStateHandler;

@Slf4j
@Service
public class AipToLegalRepJourneyHandler implements PreSubmitCallbackStateHandler<AsylumCase> {

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.NOC_REQUEST
                && HandlerUtils.isAipJourney(callback.getCaseDetails().getCaseData());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback,
                                                        PreSubmitCallbackResponse<AsylumCase> callbackResponse) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        asylumCase.remove(JOURNEY_TYPE.value());
        asylumCase.write(PREV_JOURNEY_TYPE, JourneyType.AIP);
        updateAppellantContactDetails(asylumCase);

        State currentState = callback.getCaseDetails().getState();
        if (currentState == State.AWAITING_CLARIFYING_QUESTIONS_ANSWERS) {
            currentState = asylumCase.read(PRE_CLARIFYING_STATE, State.class).orElse(currentState);
        }
        if (currentState == State.AWAITING_REASONS_FOR_APPEAL) {
            currentState = State.CASE_BUILDING;
        }
        if (currentState == State.REASONS_FOR_APPEAL_SUBMITTED) {
            currentState = State.CASE_UNDER_REVIEW;
        }

        return new PreSubmitCallbackResponse<>(asylumCase, currentState);
    }

    private void updateAppellantContactDetails(AsylumCase asylumCase) {
        Optional<List<IdValue<Subscriber>>> subscriptionsOptional = asylumCase.read(AsylumCaseFieldDefinition.SUBSCRIPTIONS);

        if (subscriptionsOptional.isPresent()) {
            // Expects subscriptions list to contain no more than one subscriber
            Subscriber subscriber = subscriptionsOptional.get().stream().findFirst().map(IdValue::getValue).orElse(null);

            if (subscriber != null) {
                ContactPreference contactPreference = subscriber.getWantsEmail() == YesOrNo.YES
                    ? ContactPreference.WANTS_EMAIL
                    : ContactPreference.WANTS_SMS;

                asylumCase.write(AsylumCaseFieldDefinition.EMAIL, subscriber.getEmail());
                asylumCase.write(AsylumCaseFieldDefinition.MOBILE_NUMBER, subscriber.getMobileNumber());
                asylumCase.write(AsylumCaseFieldDefinition.CONTACT_PREFERENCE, contactPreference);
                asylumCase.write(AsylumCaseFieldDefinition.CONTACT_PREFERENCE_DESCRIPTION, contactPreference.getDescription());

                asylumCase.clear(AsylumCaseFieldDefinition.SUBSCRIPTIONS);

                return;
            }
        }

        log.error("Subscription information is missing");
        throw new IllegalStateException("Subscription must not be null");
    }
}
