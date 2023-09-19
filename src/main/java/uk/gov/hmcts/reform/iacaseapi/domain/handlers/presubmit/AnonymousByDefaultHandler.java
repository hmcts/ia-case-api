package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_ID;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlagType.ANONYMITY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagDetail;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseFlagValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.StrategicCaseFlag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
class AnonymousByDefaultHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider systemDateProvider;

    public AnonymousByDefaultHandler(DateProvider systemDateProvider) {
        this.systemDateProvider = systemDateProvider;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT && callback.getEvent() == SUBMIT_APPEAL;
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.LATEST;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class).orElse(null);

        if (appealType == AppealType.PA || appealType == AppealType.RP) {
            Optional<StrategicCaseFlag> strategicCaseFlagOptional = asylumCase
                .read(CASE_LEVEL_FLAGS, StrategicCaseFlag.class);

            createAnonymityFlag(asylumCase, strategicCaseFlagOptional);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void createAnonymityFlag(AsylumCase asylumCase, Optional<StrategicCaseFlag> strategicCaseFlagOptional) {
        if (!hasActiveAnonimityFlag(strategicCaseFlagOptional)) {
            CaseFlagValue caseFlagValue = CaseFlagValue.builder()
                .name(ANONYMITY.getName())
                .flagCode(ANONYMITY.getFlagCode())
                .status("Active")
                .hearingRelevant(YesOrNo.YES)
                .dateTimeCreated(systemDateProvider.nowWithTime().toString())
                .build();
            List<CaseFlagDetail> caseFlagDetails = new ArrayList<>();
            String caseFlagId = asylumCase.read(CASE_FLAG_ID, String.class).orElse(UUID.randomUUID().toString());
            caseFlagDetails.add(new CaseFlagDetail(caseFlagId, caseFlagValue));

            strategicCaseFlagOptional.ifPresent(caseLevelFlags -> caseFlagDetails.addAll(caseLevelFlags.getDetails()));

            asylumCase.write(CASE_LEVEL_FLAGS, new StrategicCaseFlag(null, null, caseFlagDetails));
        }
    }

    private boolean hasActiveAnonimityFlag(@NonNull Optional<StrategicCaseFlag> strategicCaseFlag) {
        return strategicCaseFlag.map(caseFlag -> caseFlag.getDetails().stream().anyMatch(flagDetail -> {
            CaseFlagValue value = flagDetail.getCaseFlagValue();
            return Objects.equals(value.getFlagCode(), ANONYMITY.getFlagCode())
                   && Objects.equals(flagDetail.getCaseFlagValue().getStatus(), "Active");
        })).orElse(false);
    }
}
