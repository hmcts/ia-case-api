package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.UserDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

@Component
public class RequestCaseBuildingHandler implements PreSubmitCallbackHandler<AsylumCase> {

    @Autowired
    private UserDetails userDetails;
    @Autowired
    private DateProvider dateProvider;

    @Autowired
    private Appender<CaseNote> appender;

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        boolean validEvents = callback.getEvent() == Event.REQUEST_CASE_BUILDING
            || callback.getEvent() == Event.FORCE_REQUEST_CASE_BUILDING;

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && validEvents;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        asylumCase.write(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE, YesOrNo.NO);
        createAndAppendNewNote(asylumCase, callback.getEvent());
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void createAndAppendNewNote(AsylumCase asylumCase, Event event) {
        if (event.equals(Event.FORCE_REQUEST_CASE_BUILDING)) {
            Optional<List<IdValue<CaseNote>>> maybeExistingCaseNotes = asylumCase.read(CASE_NOTES);
            List<IdValue<CaseNote>> existingCaseNotes = maybeExistingCaseNotes.orElse(Collections.emptyList());
            String reason = asylumCase.read(REASON_TO_FORCE_REQUEST_CASE_BUILDING, String.class).orElse("");
            List<IdValue<CaseNote>> allCaseNotes = appender.append(buildNewCaseNote(reason), existingCaseNotes);
            asylumCase.write(CASE_NOTES, allCaseNotes);
            asylumCase.clear(REASON_TO_FORCE_REQUEST_CASE_BUILDING);
        }
    }

    private CaseNote buildNewCaseNote(String reason) {
        return new CaseNote("Force case from Awaiting Respondent Evidence to Case Building",
            reason, userDetails.getForenameAndSurname(), dateProvider.now().toString());
    }
}
