package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberGenerator;

@Service
public class AppealReferenceNumberHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String DRAFT = "DRAFT";

    private final AppealReferenceNumberGenerator appealReferenceNumberGenerator;

    public AppealReferenceNumberHandler(
        AppealReferenceNumberGenerator appealReferenceNumberGenerator
    ) {
        this.appealReferenceNumberGenerator = appealReferenceNumberGenerator;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.START_APPEAL
                   || callback.getEvent() == Event.SUBMIT_APPEAL);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        if (callback.getEvent() == Event.START_APPEAL) {
            asylumCase.setAppealReferenceNumber(DRAFT);
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            new PreSubmitCallbackResponse<>(asylumCase);

        Optional<String> existingAppealReferenceNumber = asylumCase.getAppealReferenceNumber();

        if (!existingAppealReferenceNumber.isPresent()
            || existingAppealReferenceNumber.get().equals(DRAFT)) {

            AppealType appealType =
                asylumCase
                    .getAppealType()
                    .orElseThrow(() -> new IllegalStateException("appealType is not present"));

            String appealReferenceNumber =
                appealReferenceNumberGenerator.generate(
                    callback.getCaseDetails().getId(),
                    appealType
                );

            asylumCase.setAppealReferenceNumber(appealReferenceNumber);
        }

        return callbackResponse;
    }
}
