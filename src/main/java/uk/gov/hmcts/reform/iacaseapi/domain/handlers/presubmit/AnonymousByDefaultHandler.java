package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_FLAG_ANONYMITY_EXISTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.CASE_LEVEL_FLAGS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.PAY_AND_SUBMIT_APPEAL;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SUBMIT_APPEAL;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.commondata.CaseFlagDto;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.CaseFlagMapper;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.RdCommonDataClient;

@Component
class AnonymousByDefaultHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final RdCommonDataClient rdCommonDataClient;

    AnonymousByDefaultHandler(RdCommonDataClient rdCommonDataClient) {
        this.rdCommonDataClient = rdCommonDataClient;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return
                callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                        && (callback.getEvent() == SUBMIT_APPEAL
                        || callback.getEvent() == PAY_AND_SUBMIT_APPEAL);
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        AppealType appealType = asylumCase.read(APPEAL_TYPE, AppealType.class).orElse(null);

        if (isAPorRPappeal(appealType)) {
            setAnonymityFlag(asylumCase);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private boolean isAPorRPappeal(AppealType appealType) {
        return appealType != null
                && (appealType == AppealType.PA
                || appealType == AppealType.RP);
    }

    private void setAnonymityFlag(AsylumCase asylumCase) {

        CaseFlagDto caseFlagDto = rdCommonDataClient.getStrategicCaseFlags();

        String appellantFullName = asylumCase.read(APPELLANT_NAME_FOR_DISPLAY, String.class).get();

        StrategicCaseFlag anonymityFlag =
            CaseFlagMapper.buildStrategicCaseFlagDetail(caseFlagDto.getFlags().get(0),
                StrategicCaseFlagType.RRO_ANONYMISATION, "Case", appellantFullName);

        asylumCase.write(CASE_LEVEL_FLAGS, anonymityFlag);

        asylumCase.write(CASE_FLAG_ANONYMITY_EXISTS, YesOrNo.YES);
    }
}
