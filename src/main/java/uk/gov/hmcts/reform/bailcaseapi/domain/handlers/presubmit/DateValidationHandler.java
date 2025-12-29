package uk.gov.hmcts.reform.bailcaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_ARRIVAL_IN_UK;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.APPLICANT_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.BAIL_HEARING_DATE;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_2_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_3_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_4_DOB;
import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.SUPPORTER_DOB;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.bailcaseapi.domain.handlers.PreSubmitCallbackHandler;

@Slf4j
@Component
public class DateValidationHandler implements PreSubmitCallbackHandler<BailCase> {

    private static final Set<Event> eventsToHandle = Set.of(Event.START_APPLICATION,
                                               Event.EDIT_BAIL_APPLICATION,
                                               Event.EDIT_BAIL_APPLICATION_AFTER_SUBMIT,
                                               Event.MAKE_NEW_APPLICATION);

    private static final Set<BailCaseFieldDefinition> fieldsToHandle = Set.of(APPLICANT_DOB,
                                                                              APPLICANT_ARRIVAL_IN_UK,
                                                                              BAIL_HEARING_DATE,
                                                                              SUPPORTER_DOB,
                                                                              SUPPORTER_2_DOB,
                                                                              SUPPORTER_3_DOB,
                                                                              SUPPORTER_4_DOB);

    private static final Map<String, BailCaseFieldDefinition> pageIdsToHandle = fieldsToHandle.stream()
        .collect(Collectors.toMap(BailCaseFieldDefinition::value, def -> def));

    private static final String FUTURE_DATE_ERROR_MESSAGE = "The date must not be a future date.";

    private final DateProvider dateProvider;

    public DateValidationHandler(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.MID_EVENT
               && eventsToHandle.contains(callback.getEvent())
               && pageIdsToHandle.containsKey(callback.getPageId());
    }

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLY;
    }

    public PreSubmitCallbackResponse<BailCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<BailCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        LocalDate now = dateProvider.now();

        final BailCase bailCase =
            callback
                .getCaseDetails()
                .getCaseData();

        PreSubmitCallbackResponse<BailCase> response = new PreSubmitCallbackResponse<>(bailCase);

        String pageId = callback.getPageId();
        BailCaseFieldDefinition dateField = pageIdsToHandle.get(pageId);

        String date = bailCase.read(dateField, String.class).orElse("");

        // if field is optional and left empty, move on without verifications
        if (date.isBlank() && canBeBlank(dateField)) {
            return response;
        }

        Optional<LocalDate> optionalDateToBeVerified = parseDate(date, dateField);

        optionalDateToBeVerified.ifPresent(dateToBeVerified -> {
            if (dateToBeVerified.isAfter(now)) {
                response.addError(FUTURE_DATE_ERROR_MESSAGE);
            }
        });

        return response;
    }

    // some date fields can be blank either because they're OPTIONAL in CCD or because the field stays hidden based on
    // a previous answer (e.g. has bail been refused in past 28 days? NO --> BAIL_HEARING_DATE blank)
    private boolean canBeBlank(BailCaseFieldDefinition dateField) {
        Set<BailCaseFieldDefinition> optionalDefinitions = Set.of(APPLICANT_ARRIVAL_IN_UK,
                                                                  BAIL_HEARING_DATE,
                                                                  SUPPORTER_DOB,
                                                                  SUPPORTER_2_DOB,
                                                                  SUPPORTER_3_DOB,
                                                                  SUPPORTER_4_DOB);
        return optionalDefinitions.contains(dateField);
    }

    private Optional<LocalDate> parseDate(String date, BailCaseFieldDefinition dateField) {
        try {
            return Optional.of(LocalDate.parse(date));
        } catch (DateTimeParseException e) {
            log.error("Date [{}] for field [{}] can't be parsed", date, dateField);
        }
        return Optional.empty();
    }

}
