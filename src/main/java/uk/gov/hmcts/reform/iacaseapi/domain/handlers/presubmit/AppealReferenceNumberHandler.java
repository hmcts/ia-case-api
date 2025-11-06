package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.SourceOfAppeal;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberGenerator;

import java.util.Arrays;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_SUBMISSION_INTERNAL_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ADMIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SOURCE_OF_APPEAL;

@Service
public class AppealReferenceNumberHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String DRAFT = "DRAFT";
    private final DateProvider dateProvider;
    private final AppealReferenceNumberGenerator appealReferenceNumberGenerator;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    public AppealReferenceNumberHandler(
            DateProvider dateProvider,
            AppealReferenceNumberGenerator appealReferenceNumberGenerator
    ) {
        this.dateProvider = dateProvider;
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
                .contains(callback.getEvent());
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

        if (hasNoExistingReferenceNo(asylumCase) && !isRehydratedAppeal(asylumCase)) {

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
            asylumCase.write(APPEAL_SUBMISSION_DATE, dateProvider.now().toString());

            YesOrNo isAdmin = asylumCase.read(IS_ADMIN, YesOrNo.class).orElse(YesOrNo.NO);
            if (isAdmin.equals(YesOrNo.YES)) {
                asylumCase.write(APPEAL_SUBMISSION_INTERNAL_DATE, dateProvider.now().toString());
            }
        }

        return callbackResponse;
    }

    private static boolean hasNoExistingReferenceNo(AsylumCase asylumCase) {
        Optional<String> existingAppealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER);

        return existingAppealReferenceNumber.isEmpty()
                || existingAppealReferenceNumber.get().equals(DRAFT);
    }

    private static boolean isRehydratedAppeal(AsylumCase asylumCase) {
        Optional<SourceOfAppeal> sourceOfAppeal = asylumCase.read(SOURCE_OF_APPEAL, SourceOfAppeal.class);

        return sourceOfAppeal.isPresent() && sourceOfAppeal.get() == SourceOfAppeal.REHYDRATED_APPEAL;
    }
}
