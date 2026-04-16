package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAipJourney;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.NonLegalRepDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;

@Slf4j
@Component
class SendInviteToNonLegalRepHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final IdamService idamService;

    public SendInviteToNonLegalRepHandler(IdamService idamService) {
        this.idamService = idamService;
    }


    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
            && isAipJourney(callback.getCaseDetails().getCaseData())
            && List.of(Event.SEND_INVITE_TO_NON_LEGAL_REP, Event.SUBMIT_APPEAL).contains(callback.getEvent());
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        Optional<String> nlrEmail = asylumCase.read(AsylumCaseFieldDefinition.NLR_DETAILS, NonLegalRepDetails.class)
            .map(NonLegalRepDetails::getEmailAddress);

        if (nlrEmail.isPresent() && isNull(idamService.getUserFromEmailV1(nlrEmail.get()))) {
            asylumCase.write(AsylumCaseFieldDefinition.SHOULD_INVITE_NLR_TO_IDAM, YesOrNo.YES);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
