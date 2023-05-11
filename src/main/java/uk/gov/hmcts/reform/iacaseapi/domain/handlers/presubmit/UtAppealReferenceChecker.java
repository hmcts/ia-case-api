package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.time.LocalDate.parse;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class UtAppealReferenceChecker implements PreSubmitCallbackHandler<AsylumCase> {
    private final DateProvider dateProvider;

    public UtAppealReferenceChecker(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        List<String> validPageIds = Arrays.asList("appealReference", "instructionDate");

        return (callbackStage == PreSubmitCallbackStage.MID_EVENT)
                && validPageIds.contains(callback.getPageId())
                && Event.MARK_AS_READY_FOR_UT_TRANSFER == callback.getEvent();
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();
        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);
        if (callback.getPageId().equals("appealReference")) {
            if (!isValidAppealReference(asylumCase)) {
                response.addError("Enter the Upper Tribunal reference number in the correct format.  The Upper Tribunal reference number is in the format UI-Year of submission-6 digit number, for example UI-2020-123456.");
            }
        } else if (callback.getPageId().equals("instructionDate")) {
            if (!isValidInstructionDate(asylumCase)) {
                response.addError("The date entered is not valid for When was the First-tier Tribunal instructed to transfer this appeal to the Upper Tribunal");
            }
        }
        return response;
    }

    private boolean isValidInstructionDate(AsylumCase asylumCase) {
        Optional<String> mayBeUtInstructionDate = asylumCase.read(UT_INSTRUCTION_DATE, String.class);
        LocalDate utInstructionDate =
                parse(mayBeUtInstructionDate
                        .orElseThrow(() -> new RequiredFieldMissingException("UT Instruction Date is not present")));
        return utInstructionDate != null && !utInstructionDate.isAfter(dateProvider.now());
    }

    private boolean isValidAppealReference(AsylumCase asylumCase) {
        String utAppealReference = asylumCase.read(UT_APPEAL_REFERENCE_NUMBER, String.class)
                .orElseThrow(() -> new RequiredFieldMissingException("UT Appeal Reference is missing"));
        String regexPattern = "^UI\\-\\d{4}\\-\\d{6}";
        return utAppealReference.matches(regexPattern);
    }
}


