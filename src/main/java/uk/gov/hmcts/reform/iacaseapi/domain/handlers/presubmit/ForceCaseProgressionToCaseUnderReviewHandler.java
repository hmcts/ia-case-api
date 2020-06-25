package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

@Component
public class ForceCaseProgressionToCaseUnderReviewHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final Appender<CaseNote> caseNoteAppender;
    private final DateProvider dateProvider;
    private final UserDetailsProvider userDetailsProvider;

    public ForceCaseProgressionToCaseUnderReviewHandler(
        Appender<CaseNote> caseNoteAppender,
        DateProvider dateProvider,
        @Qualifier("requestUser") UserDetailsProvider userDetailsProvider
    ) {
        this.caseNoteAppender = caseNoteAppender;
        this.dateProvider = dateProvider;
        this.userDetailsProvider = userDetailsProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.FORCE_CASE_TO_CASE_UNDER_REVIEW;
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

        String caseNoteDescription = asylumCase
            .read(REASON_TO_FORCE_CASE_TO_CASE_UNDER_REVIEW, String.class)
            .orElseThrow(() -> new IllegalStateException("reasonToForceCaseToCaseUnderReview is not present"));

        Optional<List<IdValue<CaseNote>>> maybeExistingCaseNotes =
            asylumCase.read(CASE_NOTES);

        final CaseNote newCaseNote = new CaseNote(
            "Reason for forcing case progression to case under review",
            caseNoteDescription,
            buildFullName(),
            dateProvider.now().toString()
        );

        List<IdValue<CaseNote>> allCaseNotes =
            caseNoteAppender.append(newCaseNote, maybeExistingCaseNotes.orElse(emptyList()));

        asylumCase.write(CASE_NOTES, allCaseNotes);

        asylumCase.clear(REASON_TO_FORCE_CASE_TO_CASE_UNDER_REVIEW);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String buildFullName() {
        return userDetailsProvider.getUserDetails().getForename()
            + " "
            + userDetailsProvider.getUserDetails().getSurname();
    }
}