package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.JourneyType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Service
public class AppealReferenceNumberCcdHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String DRAFT = "DRAFT";

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.SUBMIT_APPEAL
               && HandlerUtils.isAipJourney(callback.getCaseDetails().getCaseData());
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

        Optional<String> appealReferenceNumber =
            asylumCase.read(APPEAL_REFERENCE_NUMBER);

        YesOrNo isAppealReferenceNumberAvailable =
            asylumCase.read(IS_APPEAL_REFERENCE_NUMBER_AVAILABLE, YesOrNo.class)
                .orElse(YesOrNo.NO);

        if (appealReferenceNumber.isPresent()) {

            if (!appealReferenceNumber.get().equals(DRAFT) && isAppealReferenceNumberAvailable != YesOrNo.YES) {

                log.info("Appeal reference number [{}] with caseId [{}] for journeyType [{}]",
                    appealReferenceNumber.get(), callback.getCaseDetails().getId(), JourneyType.AIP);

                asylumCase.write(IS_APPEAL_REFERENCE_NUMBER_AVAILABLE, YesOrNo.YES);
            }
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
