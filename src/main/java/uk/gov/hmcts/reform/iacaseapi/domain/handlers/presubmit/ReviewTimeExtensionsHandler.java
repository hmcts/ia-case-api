package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.TimeExtensionStatus.*;

import java.text.ParseException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.*;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;

@Component
public class ReviewTimeExtensionsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final DateProvider dateProvider;

    public ReviewTimeExtensionsHandler(DateProvider dateProvider) {
        this.dateProvider = dateProvider;
    }


    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.REVIEW_TIME_EXTENSION;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage, Callback<AsylumCase> callback) {
        System.out.println("In review time extension handler");
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        //TODO Only return date is extension is granted
        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();
        String decisionOutcomeDueDate = getTimeExtensionDueDate(asylumCase);
        Date dateFormatted = null;
        try {
            dateFormatted = new SimpleDateFormat("yyyy-MM-dd").parse(decisionOutcomeDueDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (dateFormatted != null && !dateFormatted.after(new Date())) {
            PreSubmitCallbackResponse<AsylumCase> asylumCasePreSubmitCallbackResponse = new PreSubmitCallbackResponse<>(asylumCase);
            asylumCasePreSubmitCallbackResponse.addError("The new direction due date must be after the previous direction due date");
            return asylumCasePreSubmitCallbackResponse;
        }

        AtomicBoolean isGranted = new AtomicBoolean(false);


        State currentState = callback.getCaseDetails().getState();
        List<IdValue<TimeExtension>> timeExtensions = getTimeExtensions(asylumCase).map(timeExtension -> {
            TimeExtension timeExtensionValue = timeExtension.getValue();
            if (currentState == timeExtensionValue.getState() && timeExtensionValue.getStatus() == SUBMITTED) {

                TimeExtensionDecision timeExtensionDecision = getTimeExtensionDecision(asylumCase);
                TimeExtensionStatus timeExtensionStatus = timeExtensionDecision == TimeExtensionDecision.REFUSED ? REFUSED : GRANTED;
                if (timeExtensionStatus.equals(GRANTED)) {
                    isGranted.set(true);
                }
                return new IdValue<>(timeExtension.getId(), new TimeExtension(
                    timeExtensionValue.getRequestDate(),
                    timeExtensionValue.getReason(),
                    timeExtensionValue.getState(),
                    timeExtensionStatus,
                    timeExtensionValue.getEvidence(),
                    timeExtensionDecision,
                    getTimeExtensionDecisionReason(asylumCase),
                    decisionOutcomeDueDate
                ));
            }
            return timeExtension;
        }).collect(Collectors.toList());


        Optional<List<IdValue<Direction>>> maybeDirections = asylumCase.read(DIRECTIONS);
        Optional<DynamicList> dynamicList = asylumCase.read(TIME_EXTENSIONS, DynamicList.class);


            List<IdValue<Direction>> changedDirections =
                maybeDirections.orElse(emptyList())
                        .stream()
                        .map(idValue -> {

                            if (dynamicList.get().getValue().getCode().contains("Time Extension " + (maybeDirections.orElse(emptyList()).size() - (Integer.parseInt(idValue.getId())) + 1))) {
                                return new IdValue<>(
                                        idValue.getId(),
                                        new Direction(
                                                idValue.getValue().getExplanation(),
                                                idValue.getValue().getParties(),
                                                asylumCase.read(TIME_EXTENSIONS, String.class).orElse(""),
                                                dateProvider.now().toString(),
                                                idValue.getValue().getTag(),
                                                appendPreviousDates(idValue.getValue().getPreviousDates(), idValue.getValue().getDateDue(), idValue.getValue().getDateSent())
                                        )
                                );
                            } else {
                                return idValue;
                            }
                        })
                        .collect(toList());

        asylumCase.write(TIME_EXTENSIONS, timeExtensions);
        asylumCase.write(DIRECTIONS, changedDirections);
        asylumCase.write(REVIEW_TIME_EXTENSION_REQUIRED, YesOrNo.NO);


        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private TimeExtensionDecision getTimeExtensionDecision(AsylumCase asylumCase) {
        Optional<TimeExtensionDecision> read = asylumCase.read(REVIEW_TIME_EXTENSION_DECISION);
        return read.orElseThrow(() -> new IllegalArgumentException("Cannot handle " + Event.REVIEW_TIME_EXTENSION + " without a decision"));
    }

    private String getTimeExtensionDecisionReason(AsylumCase asylumCase) {
        Optional<String> read = asylumCase.read(REVIEW_TIME_EXTENSION_DECISION_REASON);
        return read.orElseThrow(() -> new IllegalArgumentException("Cannot handle " + Event.REVIEW_TIME_EXTENSION + " without a decision reason"));
    }

    private String getTimeExtensionDueDate(AsylumCase asylumCase) {
        Optional<String> read = asylumCase.read(REVIEW_TIME_EXTENSION_DUE_DATE);
        return read.orElseThrow(() -> new IllegalArgumentException("Cannot handle " + Event.REVIEW_TIME_EXTENSION + " without a decision reason"));
    }

    private List<IdValue<PreviousDates>> appendPreviousDates(List<IdValue<PreviousDates>> previousDates, String dateDue, String dateSent) {

        if (CollectionUtils.isEmpty(previousDates)) {

            return newArrayList(new IdValue<>("1", new PreviousDates(dateDue, dateSent)));
        } else {

            int index = previousDates.size() + 1;

            final List<IdValue<PreviousDates>> allPreviousDates = new ArrayList<>();
            allPreviousDates.add(new IdValue<>(String.valueOf(index--), new PreviousDates(dateDue, dateSent)));

            for (IdValue<PreviousDates> previousDate : previousDates) {
                allPreviousDates.add(new IdValue<>(String.valueOf(index--), previousDate.getValue()));
            }

            return allPreviousDates;
        }
    }

        private Stream<IdValue<TimeExtension>> getTimeExtensions(AsylumCase asylumCase) {
        return asylumCase.<List<IdValue<TimeExtension>>>read(TIME_EXTENSIONS)
            .orElse(emptyList()).stream();
    }
}
