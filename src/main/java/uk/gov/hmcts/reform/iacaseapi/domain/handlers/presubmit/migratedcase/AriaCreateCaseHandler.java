package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit.migratedcase;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_REFERENCE_NUMBER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_SUBMISSION_DATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_DETENTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_DESIRED_STATE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_DESIRED_STATE_SELECTED_VALUE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.ARIA_MIGRATION_TASK_DUE_DAYS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ARIA_MIGRATED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_ARIA_MIGRATED_FILTER;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberGenerator;

@Component
public class AriaCreateCaseHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;
    private final AppealReferenceNumberGenerator appealReferenceNumberGenerator;

    public AriaCreateCaseHandler(DateProvider dateProvider,
                                 AppealReferenceNumberGenerator appealReferenceNumberGenerator) {
        this.dateProvider = dateProvider;
        this.appealReferenceNumberGenerator = appealReferenceNumberGenerator;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {

        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        return (callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && Event.ARIA_CREATE_CASE == callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        String ariaTaskDueDays = asylumCase.read(ARIA_MIGRATION_TASK_DUE_DAYS, String.class).orElse(null);

        if (ariaTaskDueDays == null || ariaTaskDueDays.isBlank()) {

            PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse =
                    new PreSubmitCallbackResponse<>(asylumCase);

            asylumCasePreSubmitCallbackResponse
                    .addError("You must provide ariaMigrationTaskDueDays as part of the case creation.");
            return asylumCasePreSubmitCallbackResponse;
        }

        AppealType appealType =
            asylumCase
                .read(APPEAL_TYPE, AppealType.class)
                .orElseThrow(() -> new IllegalStateException("appealType is not present"));

        boolean isDetainedAppeal = asylumCase.read(APPELLANT_IN_DETENTION, YesOrNo.class).orElse(NO) == YES;

        String appealReferenceNumber =
            appealReferenceNumberGenerator.generate(
                callback.getCaseDetails().getId(),
                appealType,
                isDetainedAppeal
            );

        asylumCase.write(APPEAL_REFERENCE_NUMBER, appealReferenceNumber);
        asylumCase.write(APPEAL_SUBMISSION_DATE, dateProvider.now().toString());
        asylumCase.write(IS_ARIA_MIGRATED, YesOrNo.YES);
        //isAriaMigratedFilter is used separately for case list filtering on ExUI
        asylumCase.write(IS_ARIA_MIGRATED_FILTER, YesOrNo.YES);
        asylumCase.read(ARIA_DESIRED_STATE, State.class).ifPresent(value -> asylumCase.write(ARIA_DESIRED_STATE_SELECTED_VALUE, value.getDescription()));

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}