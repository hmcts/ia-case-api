package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.RECORD_ALLOCATED_JUDGE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;

@Component
public class RecordAllocatedJudgeHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final Appender<String> appender;

    public RecordAllocatedJudgeHandler(Appender<String> appender) {
        this.appender = appender;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage.equals(ABOUT_TO_SUBMIT) && callback.getEvent().equals(RECORD_ALLOCATED_JUDGE);
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


        String allocatedJudge = asylumCase
            .read(ALLOCATED_JUDGE_EDIT, String.class)
            .orElseThrow(() -> new IllegalArgumentException("there is no Allocated Judge in request"));

        asylumCase.read(ALLOCATED_JUDGE, String.class).ifPresent(previousJudge ->

            asylumCase.write(
                AsylumCaseFieldDefinition.PREVIOUS_JUDGE_ALLOCATIONS,
                appender.append(
                    previousJudge,
                    asylumCase
                        .<List<IdValue<String>>>read(AsylumCaseFieldDefinition.PREVIOUS_JUDGE_ALLOCATIONS)
                        .orElse(Collections.emptyList())
                )
            )
        );

        asylumCase.write(ALLOCATED_JUDGE, allocatedJudge);
        asylumCase.write(JUDGE_ALLOCATION_EXISTS, YesOrNo.YES);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
