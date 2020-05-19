package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.TimeExtensionAppender;

@Component
public class TimeExtensionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final TimeExtensionAppender timeExtensionAppender;

    public TimeExtensionHandler(
        TimeExtensionAppender timeExtensionAppender
    ) {
        this.timeExtensionAppender = timeExtensionAppender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return
            callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && Arrays.asList(
                Event.SUBMIT_TIME_EXTENSION
            ).contains(callback.getEvent());
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

        String submitTimeExtensionReason =
            asylumCase
                .read(SUBMIT_TIME_EXTENSION_REASON, String.class)
                .orElseThrow(() -> new IllegalStateException("timeExtensionReason is not present"));

        Optional<List<IdValue<Document>>> submitTimeExtensionEvidence =
            asylumCase
                .read(SUBMIT_TIME_EXTENSION_EVIDENCE);

        final State caseState =
            callback
                .getCaseDetails()
                .getState();

        Optional<List<IdValue<TimeExtension>>> maybeTimeExtensions =
            asylumCase.read(TIME_EXTENSIONS);

        final List<IdValue<TimeExtension>> existingTimeExtensions =
            maybeTimeExtensions.orElse(emptyList());

        List<IdValue<TimeExtension>> allTimeExtensions =
            timeExtensionAppender.append(
                existingTimeExtensions,
                caseState,
                submitTimeExtensionReason,
                submitTimeExtensionEvidence.orElse(emptyList())
            );

        asylumCase.write(TIME_EXTENSIONS, allTimeExtensions);

        asylumCase.clear(SUBMIT_TIME_EXTENSION_REASON);
        asylumCase.clear(SUBMIT_TIME_EXTENSION_EVIDENCE);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
