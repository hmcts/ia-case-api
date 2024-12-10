package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.TtlDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DeletionDateProvider;

import java.time.LocalDate;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.TTL;

@Slf4j
@Service
public class AppealSetDraftTtlHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final DeletionDateProvider deletionDateProvider;

    public AppealSetDraftTtlHandler(DeletionDateProvider deletionDateProvider) {
        this.deletionDateProvider = deletionDateProvider;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        LocalDate deletionDate = deletionDateProvider.getDeletionDate();

        asylumCase.write(AsylumCaseFieldDefinition.TTL,
            TtlDetails.builder()
                .systemSetTtl(deletionDate)
                .manualTtlOverride(deletionDate)
                .doNotDelete(YesOrNo.YES)
                .build()
        );

        Optional<TtlDetails> ttlOpt = asylumCase.read(AsylumCaseFieldDefinition.TTL, TtlDetails.class);
        if (ttlOpt.isPresent()) {
            TtlDetails ttl = ttlOpt.get();
            log.info(
                    "Setting deletionDate when starting appeal, caseId {}, systemSetTtl {}, manualTtlOverride {}," +
                            " doNotDelete {}",
                    callback.getCaseDetails().getId(),
                    ttl.getSystemSetTtl(),
                    ttl.getManualTtlOverride(),
                    ttl.getDoNotDelete()
            );
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.START_APPEAL;
    }
}
