package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

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
import uk.gov.hmcts.reform.iacaseapi.domain.service.CcdDataService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.IdamService;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_NON_LEGAL_REP_JOINED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.IS_SPONSOR_SAME_AS_NLR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.JOIN_APPEAL_PIN;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.NLR_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isAipJourney;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.updateSubscriptionsForNlr;

@Slf4j
@Component
class SendInviteToNonLegalRepHandler implements PreSubmitCallbackHandler<AsylumCase> {
    private final IdamService idamService;
    private final CcdDataService ccdDataService;

    public SendInviteToNonLegalRepHandler(IdamService idamService,
                                          CcdDataService ccdDataService) {
        this.idamService = idamService;
        this.ccdDataService = ccdDataService;
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

        if (nlrEmail.isPresent() && isNull(idamService.getUserFromEmail(nlrEmail.get()))) {
            asylumCase.write(AsylumCaseFieldDefinition.SHOULD_INVITE_NLR_TO_IDAM, YesOrNo.YES);
        }

        if (callback.getEvent().equals(Event.SEND_INVITE_TO_NON_LEGAL_REP)) {
            String email = asylumCase.read(AsylumCaseFieldDefinition.NLR_DETAILS, NonLegalRepDetails.class)
                .map(NonLegalRepDetails::getEmailAddress)
                .orElseThrow(() -> new IllegalArgumentException("NLR email address is not present in the case"));
            callback.getCaseDetailsBefore().flatMap(caseDetailsBefore ->
                    caseDetailsBefore.getCaseData().read(NLR_DETAILS, NonLegalRepDetails.class))
                .ifPresent((nonLegalRepDetails) -> {
                    if (nonLegalRepDetails.getIdamId() != null) {
                        ccdDataService.revokeUserAccessToCase(callback.getCaseDetails().getId(), nonLegalRepDetails.getIdamId());
                    }
                });
            asylumCase.write(NLR_DETAILS, NonLegalRepDetails.builder().emailAddress(email).build());
            asylumCase.clear(JOIN_APPEAL_PIN);
            asylumCase.clear(IS_SPONSOR_SAME_AS_NLR);
            asylumCase.clear(HAS_NON_LEGAL_REP_JOINED);
            updateSubscriptionsForNlr(asylumCase);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
