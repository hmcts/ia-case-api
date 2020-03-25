package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_NOTES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.REASON_TO_FORCE_REQUEST_CASE_BUILDING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.UserDetailsProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseNote;
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
    private UserDetailsProvider userDetailsProvider;

    @Autowired
    private Appender<CaseNote> appender;

    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && callback.getEvent() == Event.REQUEST_CASE_BUILDING;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        asylumCase.write(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE, YesOrNo.NO);
        createAndAppendNewNote(asylumCase);
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void createAndAppendNewNote(AsylumCase asylumCase) {
        String reason = asylumCase.read(REASON_TO_FORCE_REQUEST_CASE_BUILDING, String.class).orElse(null);
        if (reason != null) {
            Optional<List<IdValue<CaseNote>>> maybeExistingCaseNotes = asylumCase.read(CASE_NOTES);
            List<IdValue<CaseNote>> existingCaseNotes = maybeExistingCaseNotes.orElse(Collections.emptyList());
            List<IdValue<CaseNote>> allCaseNotes = appender.append(buildNewCaseNote(reason), existingCaseNotes);
            asylumCase.write(CASE_NOTES, allCaseNotes);
            asylumCase.clear(REASON_TO_FORCE_REQUEST_CASE_BUILDING);
        }
    }

    private CaseNote buildNewCaseNote(String reason) {
        return new CaseNote("Force case from Awaiting Respondent Evidence to Case Building",
            reason, userDetailsProvider.getUserDetails().getForenameAndSurname(), LocalDate.now().toString());
    }
}
