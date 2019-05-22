package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumExtractor.APPEAL_TYPE;

import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberGenerator;

@Service
public class AppealReferenceNumberHandler implements PreSubmitCallbackHandler<CaseDataMap> {

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
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && (callback.getEvent() == Event.START_APPEAL
                   || callback.getEvent() == Event.SUBMIT_APPEAL);
    }

    @Override
    public PreSubmitCallbackResponse<CaseDataMap> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        CaseDataMap caseDataMap =
            callback
                .getCaseDetails()
                .getCaseData();

        if (callback.getEvent() == Event.START_APPEAL) {
            caseDataMap.write(APPEAL_REFERENCE_NUMBER, DRAFT);
            return new PreSubmitCallbackResponse<>(caseDataMap);
        }

        PreSubmitCallbackResponse<CaseDataMap> callbackResponse =
            new PreSubmitCallbackResponse<>(caseDataMap);

        Optional<String> existingAppealReferenceNumber = caseDataMap.get(APPEAL_REFERENCE_NUMBER);

        if (!existingAppealReferenceNumber.isPresent()
            || existingAppealReferenceNumber.get().equals(DRAFT)) {

            AppealType appealType =
                caseDataMap
                    .get(APPEAL_TYPE, AppealType.class)
                    .orElseThrow(() -> new IllegalStateException("appealType is not present"));

            String appealReferenceNumber =
                appealReferenceNumberGenerator.generate(
                    callback.getCaseDetails().getId(),
                    appealType
                );

            caseDataMap.write(APPEAL_REFERENCE_NUMBER, appealReferenceNumber);
        }

        return callbackResponse;
    }
}
