package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.DETENTION_FACILITY;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.IRC_NAME;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.PRISON_NAME;

import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class DetentionFacilityAppender implements PreSubmitCallbackHandler<BailCase> {

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && getEventsToHandle().contains(callback.getEvent());
    }

    private List<Event> getEventsToHandle() {
        List<Event> eventsToHandle = Lists.newArrayList(
            Event.START_APPLICATION,
            Event.MAKE_NEW_APPLICATION,
            Event.EDIT_BAIL_APPLICATION,
            Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT
        );
        return eventsToHandle;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        String ircValue = bailCase.read(IRC_NAME, String.class).orElse("");
        String prisonValue = bailCase.read(PRISON_NAME, String.class).orElse("");

        if (!ircValue.isEmpty()) {
            bailCase.write(DETENTION_FACILITY, ircValue);

        } else if (!prisonValue.isEmpty()) {
            bailCase.write(DETENTION_FACILITY, prisonValue);

        } else {
            throw new RequiredFieldMissingException("Prison name and IRC name missing");
        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }

}
