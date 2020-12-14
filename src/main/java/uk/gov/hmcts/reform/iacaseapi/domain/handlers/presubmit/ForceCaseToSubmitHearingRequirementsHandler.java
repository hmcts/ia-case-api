package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

@Component
public class ForceCaseToSubmitHearingRequirementsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final Appender<CaseNote> caseNoteAppender;
    private final DateProvider dateProvider;
    private final UserDetails userDetails;

    public ForceCaseToSubmitHearingRequirementsHandler(
        Appender<CaseNote> caseNoteAppender,
        DateProvider dateProvider,
        UserDetails userDetails
    ) {
        this.caseNoteAppender = caseNoteAppender;
        this.dateProvider = dateProvider;
        this.userDetails = userDetails;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == ABOUT_TO_SUBMIT
            && callback.getEvent() == FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS;
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
            .read(REASON_TO_FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS, String.class)
            .orElseThrow(() -> new IllegalStateException("reasonToForceCaseToSubmitHearingRequirements is not present"));

        Optional<List<IdValue<CaseNote>>> maybeExistingCaseNotes =
            asylumCase.read(CASE_NOTES);

        final CaseNote newCaseNote = new CaseNote(
            "Reason for forcing the case progression to submit hearing requirements",
            caseNoteDescription,
            buildFullName(),
            dateProvider.now().toString()
        );

        List<IdValue<CaseNote>> allCaseNotes =
            caseNoteAppender.append(newCaseNote, maybeExistingCaseNotes.orElse(emptyList()));

        asylumCase.write(CASE_NOTES, allCaseNotes);

        asylumCase.clear(REASON_TO_FORCE_CASE_TO_SUBMIT_HEARING_REQUIREMENTS);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private String buildFullName() {
        return userDetails.getForename()
            + " "
            + userDetails.getSurname();
    }
}
