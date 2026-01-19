package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberGenerator;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.sourceOfAppealRehydratedAppeal;

@Service
@Slf4j
public class AppealReferenceNumberHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String DRAFT = "DRAFT";
    private final AppealReferenceNumberGenerator appealReferenceNumberGenerator;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

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
                && Arrays.asList(
                        Event.START_APPEAL,
                        Event.SUBMIT_APPEAL)
                .contains(callback.getEvent())
                && !sourceOfAppealRehydratedAppeal(callback.getCaseDetails().getCaseData());
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
            asylumCase.write(APPEAL_REFERENCE_NUMBER, DRAFT);
            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
                new PreSubmitCallbackResponse<>(asylumCase);

        if (hasNoExistingReferenceNo(asylumCase)) {
            AppealType appealType =
                    asylumCase
                            .read(APPEAL_TYPE, AppealType.class)
                            .orElseThrow(() -> new IllegalStateException("appealType is not present"));

            String appealReferenceNumber =
                    appealReferenceNumberGenerator.generate(
                            callback.getCaseDetails().getId(),
                            appealType
                    );

            asylumCase.write(APPEAL_REFERENCE_NUMBER, appealReferenceNumber);
        }

        return callbackResponse;
    }

    private static boolean hasNoExistingReferenceNo(AsylumCase asylumCase) {
        Optional<String> existingAppealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER);

        return existingAppealReferenceNumber.isEmpty()
                || existingAppealReferenceNumber.get().equals(DRAFT);
    }
}
