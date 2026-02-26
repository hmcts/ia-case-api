package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithDescription;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_THE_NOTICE_OF_DECISION_DOCS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_THE_NOTICE_OF_DECISION_DOCS_REHYDRATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isRehydratedAppeal;

@Component
public class RehydratedNoticeOfDecisionUploadHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final List<Event> APPLICABLE_EVENTS = List.of(
            Event.START_APPEAL,
            Event.EDIT_APPEAL,
            Event.EDIT_APPEAL_AFTER_SUBMIT
    );

    @Override
    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT &&
                APPLICABLE_EVENTS.contains(callback.getEvent()) &&
                isRehydratedAppeal(callback.getCaseDetails().getCaseData());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        Optional<List<IdValue<DocumentWithDescription>>> rehydratedDocs =
                asylumCase.read(UPLOAD_THE_NOTICE_OF_DECISION_DOCS_REHYDRATED);

        Optional<List<IdValue<DocumentWithDescription>>> existingDocs =
                asylumCase.read(UPLOAD_THE_NOTICE_OF_DECISION_DOCS);

        if (rehydratedDocs.isPresent()) {

            if (existingDocs.isEmpty()) {
                asylumCase.write(UPLOAD_THE_NOTICE_OF_DECISION_DOCS, rehydratedDocs.get());
            }

            asylumCase.clear(UPLOAD_THE_NOTICE_OF_DECISION_DOCS_REHYDRATED);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
