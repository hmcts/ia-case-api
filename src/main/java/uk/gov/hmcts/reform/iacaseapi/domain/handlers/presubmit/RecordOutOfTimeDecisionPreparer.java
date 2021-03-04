package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.PREVIOUS_OUT_OF_TIME_DECISION_DETAILS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfTimeDecisionDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.OutOfTimeDecisionType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.Document;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.OutOfTimeDecisionDetailsAppender;

@Component
public class RecordOutOfTimeDecisionPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private OutOfTimeDecisionDetailsAppender outOfTimeDecisionDetailsAppender;

    public RecordOutOfTimeDecisionPreparer(OutOfTimeDecisionDetailsAppender outOfTimeDecisionDetailsAppender) {
        this.outOfTimeDecisionDetailsAppender = outOfTimeDecisionDetailsAppender;
    }

    @Override
    public boolean canHandle(PreSubmitCallbackStage callbackStage,
                             Callback<AsylumCase> callback) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.RECORD_OUT_OF_TIME_DECISION;
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(PreSubmitCallbackStage callbackStage,
                                                        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        YesOrNo recordedOutOfTimeDecision = asylumCase.read(RECORDED_OUT_OF_TIME_DECISION, YesOrNo.class).orElse(NO);

        if (recordedOutOfTimeDecision == YES) {

            Optional<List<IdValue<OutOfTimeDecisionDetails>>> maybeOutOfTimeDecisionDetails =
                asylumCase.read(PREVIOUS_OUT_OF_TIME_DECISION_DETAILS);

            final List<IdValue<OutOfTimeDecisionDetails>> existingOutOfTimeDecisionDetails =
                maybeOutOfTimeDecisionDetails.orElse(Collections.emptyList());

            OutOfTimeDecisionType outOfTimeDecisionType =
                asylumCase.read(OUT_OF_TIME_DECISION_TYPE, OutOfTimeDecisionType.class)
                    .orElseThrow(() -> new IllegalStateException("Out of time decision type is not present"));

            String outOfTimeDecisionMaker =
                asylumCase.read(OUT_OF_TIME_DECISION_MAKER, String.class)
                    .orElseThrow(() -> new IllegalStateException("Out of time decision maker is not present"));

            Optional<Document> outOfTimeDecisionDocument = asylumCase.read(OUT_OF_TIME_DECISION_DOCUMENT);

            String outOfTimeDecisionTypeDescription = null;
            switch (outOfTimeDecisionType) {
                case IN_TIME:
                    outOfTimeDecisionTypeDescription = "Appeal is in time";
                    break;

                case APPROVED:
                    outOfTimeDecisionTypeDescription = "Appeal is out of time but can proceed";
                    break;

                case REJECTED:
                    outOfTimeDecisionTypeDescription = "Appeal is out of time and cannot proceed";
                    break;

                default:
                    break;
            }

            if (outOfTimeDecisionDocument.isPresent()) {

                final OutOfTimeDecisionDetails outOfTimeDecisionDetails =
                    new OutOfTimeDecisionDetails(outOfTimeDecisionTypeDescription, outOfTimeDecisionMaker, outOfTimeDecisionDocument.get());

                outOfTimeDecisionDetailsAppender.append(existingOutOfTimeDecisionDetails, outOfTimeDecisionDetails);
            }

            asylumCase.clear(OUT_OF_TIME_DECISION_TYPE);
            asylumCase.clear(OUT_OF_TIME_DECISION_MAKER);
            asylumCase.clear(OUT_OF_TIME_DECISION_DOCUMENT);
        }


        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}
