
package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionStatus.*;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionDecision;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionStatus;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ReviewTimeExtensionsHandler implements PreSubmitCallbackHandler<AsylumCase> {
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getEvent() == Event.REVIEW_TIME_EXTENSION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        System.out.println("In review time extension handler");
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        //TODO Only return date is extension is granted
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<String> dueDate = asylumCase.read(REVIEW_TIME_EXTENSION_DUE_DATE);
        Date dateFormatted = null;
        try {
            dateFormatted = new SimpleDateFormat("yyyy-MM-dd").parse(dueDate.get());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (dateFormatted != null && !dateFormatted.after(new Date())) {
            PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
            asylumCasePreSubmitCallbackResponse.addError("The new direction due date must be after the previous direction due date");
            return asylumCasePreSubmitCallbackResponse;
        }

        State currentState = callback.getCaseDetails().getState();
        List<IdValue<TimeExtension>> timeExtensions = getTimeExtensions(asylumCase).map(timeExtension -> {
            TimeExtension timeExtensionValue = timeExtension.getValue();
            if (currentState == timeExtensionValue.getState() && timeExtensionValue.getStatus() == SUBMITTED) {

                TimeExtensionDecision timeExtensionDecision = getTimeExtensionDecision(asylumCase);
                TimeExtensionStatus timeExtensionStatus = timeExtensionDecision == TimeExtensionDecision.REFUSED ? REFUSED : GRANTED;

                return new IdValue<>(timeExtension.getId(), new TimeExtension(
                        timeExtensionValue.getRequestedDate(),
                        timeExtensionValue.getReason(),
                        timeExtensionValue.getState(),
                        timeExtensionStatus,
                        timeExtensionValue.getEvidence(),
                        timeExtensionDecision,
                        getTimeExtensionDecisionReason(asylumCase)
                ));
            }
            return timeExtension;
        }).collect(Collectors.toList());

        asylumCase.write(TIME_EXTENSIONS, timeExtensions);
        asylumCase.write(REVIEW_TIME_EXTENSION_REQUIRED, YesOrNo.NO);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private TimeExtensionDecision getTimeExtensionDecision(AsylumCase asylumCase) {
        Optional<TimeExtensionDecision> read = asylumCase.read(REVIEW_TIME_EXTENSION_DECISION);
        return read.orElseThrow(() -> new IllegalArgumentException("Cannot handle " + Event.REVIEW_TIME_EXTENSION + " without a decision"));
    }

    private String getTimeExtensionDecisionReason(AsylumCase asylumCase) {
        Optional<String> read = asylumCase.read(REVIEW_TIME_EXTENSION_DECISION_REASON);
        return read.orElseThrow(() -> new IllegalArgumentException("Cannot handle " + Event.REVIEW_TIME_EXTENSION + " without a decision reason"));
    }

    private Stream<IdValue<TimeExtension>> getTimeExtensions(AsylumCase asylumCase) {
        return asylumCase.<List<IdValue<TimeExtension>>>read(TIME_EXTENSIONS)
                .orElse(Collections.emptyList()).stream();
    }
}
