package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.BAIL_REFERENCE_NUMBER;

import java.text.ParseException;
import java.util.Optional;
import javax.swing.text.MaskFormatter;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class BailReferenceNumberHandler implements PreSubmitCallbackHandler<BailCase> {

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.START_APPLICATION;
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

        Optional<String> existingBailReferenceNumber = bailCase.read(BAIL_REFERENCE_NUMBER);

        if (!existingBailReferenceNumber.isPresent() || existingBailReferenceNumber.get().equals("DRAFT")) {

            long caseId = callback.getCaseDetails().getId();

            String caseIdMask = "####-####-####-####";
            String caseIdString = String.valueOf(caseId);

            MaskFormatter maskFormatter;

            try {

                maskFormatter = new MaskFormatter(caseIdMask);
                maskFormatter.setValueContainsLiteralCharacters(false);

                String caseIdStringFormatted = maskFormatter.valueToString(caseIdString);
                bailCase.write(BAIL_REFERENCE_NUMBER, caseIdStringFormatted);

            } catch (ParseException e) {

                throw new RuntimeException("Error parsing bail reference number" + caseId);

            }

        }

        return new PreSubmitCallbackResponse<>(bailCase);
    }

}
