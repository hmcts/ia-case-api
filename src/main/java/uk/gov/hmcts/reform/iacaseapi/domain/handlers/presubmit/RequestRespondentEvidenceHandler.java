package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CALCULATED_HEARING_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ACCELERATED_DETAINED_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LISTING_AVAILABLE_FOR_ADA;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.*;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DueDateService;

@Component
public class RequestRespondentEvidenceHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private DueDateService dueDateService;

    public RequestRespondentEvidenceHandler(DueDateService dueDateService) {
        this.dueDateService = dueDateService;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.REQUEST_RESPONDENT_EVIDENCE;
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

        // Set a new flag here to be used for validation in the preparer.
        asylumCase.write(UPLOAD_HOME_OFFICE_BUNDLE_AVAILABLE, YES);

        if (isAcceleratedDetainedAppeal(asylumCase)) {
            // Set flag used to allow ListCase to be visible in awaitingRespondentEvidence state when
            // case is accelerated detained appeal
            asylumCase.write(LISTING_AVAILABLE_FOR_ADA, YES);

            // Calculate date to display on Overview tab
            String calculatedListedHearingDate = dueDateService.calculateDueDate(ZonedDateTime.now(), 22)
                .format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
            asylumCase.write(CALCULATED_HEARING_DATE, calculatedListedHearingDate);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isAcceleratedDetainedAppeal(AsylumCase asylumCase) {
        return asylumCase.read(IS_ACCELERATED_DETAINED_APPEAL, YesOrNo.class)
            .orElse(NO)
            .equals(YES);
    }
}
