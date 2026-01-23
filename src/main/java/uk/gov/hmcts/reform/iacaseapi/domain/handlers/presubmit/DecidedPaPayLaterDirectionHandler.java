package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.*;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;

@Component
public class DecidedPaPayLaterDirectionHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final int hearingRequirementsDueInDays;
    private final DateProvider dateProvider;
    private final DirectionAppender directionAppender;

    public DecidedPaPayLaterDirectionHandler(
            @Value("${legalRepresentativeHearingRequirements.dueInDays}") int hearingRequirementsDueInDays,
            DateProvider dateProvider,
            DirectionAppender directionAppender
    ) {
        this.hearingRequirementsDueInDays = hearingRequirementsDueInDays;
        this.dateProvider = dateProvider;
        this.directionAppender = directionAppender;
    }

    public boolean canHandle(
            PreSubmitCallbackStage callbackStage,
            Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        String paAppealTypePaymentOption = asylumCase.read(PA_APPEAL_TYPE_PAYMENT_OPTION, String.class).orElse("");
        String paAppealTypeAipPaymentOption = asylumCase.read(PA_APPEAL_TYPE_AIP_PAYMENT_OPTION, String.class).orElse("");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
                && callback.getCaseDetails().getState() == State.DECIDED
                && (paAppealTypePaymentOption == "payLater" || paAppealTypeAipPaymentOption == "payLater")
                && (HandlerUtils.isAipJourney(callback.getCaseDetails().getCaseData())
                || HandlerUtils.isLegalRepJourney(callback.getCaseDetails().getCaseData()));
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

        ZonedDateTime scheduledDate = ZonedDateTime.of(dateProvider.nowWithTime(), ZoneId.systemDefault()).plusMinutes(schedule7DaysInMinutes);

        TimedEvent timedEvent = scheduler.schedule(
                new TimedEvent(
                        "",
                        Event.SEND_PAYMENT_REMINDER_NOTIFICATION,
                        scheduledDate,
                        "IA",
                        "Asylum",
                        callback.getCaseDetails().getId()
                )
        );

        Optional<List<IdValue<Direction>>> maybeDirections = asylumCase.read(DIRECTIONS);

        final List<IdValue<Direction>> existingDirections =
                maybeDirections.orElse(emptyList());

        List<IdValue<Direction>> allDirections =
                directionAppender.append(
                        asylumCase,
                        existingDirections,
                        "Your appeal has now been decided but still have not paid your fee. The tribunal has sent two notifications regarding the outstanding amount. If you do not pay [HEARING_METHOD] (Oral/Paper amount) the Tribunal may instigate legal proceedings to recover the fee.\n" +
                                "Instructions for making a payment are: \n" +
                                "For appeals submitted online \n" +
                                "(Legal Representative to make payment by PBA)\n" +
                                "Sign in to your account at: Sign in to the service if you’ve already started your appeal..\n" +
                                "2. Select 'Pay for this appeal' under the 'I want to' section and follow the steps to make a new payment.\n" +
                                "For appeals submitted by post or email \n" +
                                "Follow these steps to pay the fee:\n" +
                                "1. Call the tribunal on +44 (0)300 123 1711, then select option 3 \n" +
                                "2. Provide your 16-digit online case reference number:\n" +
                                "3. Make the payment with a debit or credit card\n",
                        getParty(asylumCase),
                        dateProvider
                                .now()
                                .plusDays(hearingRequirementsDueInDays)
                                .toString(),
                        DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS,
                        callback.getEvent().toString()
                );

        asylumCase.write(DIRECTIONS, allDirections);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private Parties getParty(AsylumCase asylumCase) {
        if (HandlerUtils.isAipJourney(asylumCase)) {
            return Parties.APPELLANT;
        }
        else if (HandlerUtils.isLegalRepJourney(asylumCase)) {
            return Parties.LEGAL_REPRESENTATIVE;
        }
    }
}
