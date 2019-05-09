package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.CaseDataMap;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DirectionTag;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Parties;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.DirectionAppender;

@Component
public class LegalRepresentativeHearingRequirementsDirectionHandler implements PreSubmitCallbackHandler<CaseDataMap> {

    private final int hearingRequirementsDueInDays;
    private final DateProvider dateProvider;
    private final DirectionAppender directionAppender;

    public LegalRepresentativeHearingRequirementsDirectionHandler(
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
        Callback<CaseDataMap> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.REQUEST_HEARING_REQUIREMENTS;
    }

    public PreSubmitCallbackResponse<CaseDataMap> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<CaseDataMap> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        CaseDataMap CaseDataMap =
            callback
                .getCaseDetails()
                .getCaseData();

        final List<IdValue<Direction>> existingDirections =
            CaseDataMap
                .getDirections()
                .orElse(Collections.emptyList());

        List<IdValue<Direction>> allDirections =
            directionAppender.append(
                existingDirections,
                "Your appeal is going to a hearing. Login to submit your hearing requirements on the overview tab.\n\n"
                + "Next steps\n"
                + "The case officer will review your hearing requirements and try to accommodate them. "
                + "You will then be sent a hearing date.\n"
                + "If you do not supply your hearing requirements within " + hearingRequirementsDueInDays + " days, "
                + "we may not be able to accommodate your needs for the hearing.\n",
                Parties.LEGAL_REPRESENTATIVE,
                dateProvider
                    .now()
                    .plusDays(hearingRequirementsDueInDays)
                    .toString(),
                DirectionTag.LEGAL_REPRESENTATIVE_HEARING_REQUIREMENTS
            );

        CaseDataMap.setDirections(allDirections);

        return new PreSubmitCallbackResponse<>(CaseDataMap);
    }
}
