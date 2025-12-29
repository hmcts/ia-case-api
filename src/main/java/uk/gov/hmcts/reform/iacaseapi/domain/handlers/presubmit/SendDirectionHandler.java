package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.DATE_OF_COMPLIANCE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.DIRECTIONS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.LISTING_EVENT;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_DESCRIPTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCaseFieldDefinition.SEND_DIRECTION_LIST;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ListingEvent.INITIAL_LISTING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.CASE_LISTING;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event.SEND_BAIL_DIRECTION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage.ABOUT_TO_SUBMIT;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Direction;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ListingEvent;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.Appender;


@Component
public class SendDirectionHandler implements PreSubmitCallbackHandler<BailCase> {

    private final Appender<Direction> directionAppender;
    private final DateProvider dateProvider;

    public SendDirectionHandler(
        Appender<Direction> directionAppender,
        DateProvider dateProvider
    ) {
        this.directionAppender = directionAppender;
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        BailCase bailCase = callback.getCaseDetails().getCaseData();

        final ListingEvent listingEvent = bailCase.read(LISTING_EVENT, ListingEvent.class)
            .orElse(null);

        return callbackStage.equals(ABOUT_TO_SUBMIT)
            && (callback.getEvent().equals(SEND_BAIL_DIRECTION)
            || (callback.getEvent().equals(CASE_LISTING) && INITIAL_LISTING == listingEvent));
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        String sendDirectionDescription = bailCase
                .read(SEND_DIRECTION_DESCRIPTION, String.class)
                .orElseThrow(() -> new IllegalStateException("sendDirectionDescription is not present"));

        String sendDirectionList = bailCase
                .read(SEND_DIRECTION_LIST, String.class)
                .orElseThrow(() -> new IllegalStateException("sendDirectionList is not present"));

        String dateOfCompliance = bailCase
                .read(DATE_OF_COMPLIANCE, String.class)
                .orElseThrow(() -> new IllegalStateException("dateOfCompliance is not present"));


        Optional<List<IdValue<Direction>>> maybeExistingDirections =
            bailCase.read(DIRECTIONS);

        LocalDate now = dateProvider.now();

        final Direction newDirection = new Direction(
            sendDirectionDescription,
            sendDirectionList,
            dateOfCompliance,
            now.toString(),
            dateProvider.nowWithTime().toString(),
            null,
            Collections.emptyList()
            );

        List<IdValue<Direction>> allDirections =
            directionAppender.append(newDirection, maybeExistingDirections.orElse(emptyList()));

        PreSubmitCallbackResponse<BailCase> response = new PreSubmitCallbackResponse<>(bailCase);

        LocalDate dateSelected = LocalDate.parse(dateOfCompliance);

        if (!dateSelected.isAfter(now)) {
            response.addError("The date they must comply by must be a future date.");
        } else {
            bailCase.write(DIRECTIONS, allDirections);
        }

        bailCase.clear(SEND_DIRECTION_DESCRIPTION);
        bailCase.clear(SEND_DIRECTION_LIST);
        bailCase.clear(DATE_OF_COMPLIANCE);

        return response;
    }

}
