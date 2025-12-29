package uk.gov.hmcts.reform.iacaseapi.infrastructure.eventvalidation;

import static uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCaseFieldDefinition.UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.BailCase;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.State;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.bailcaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.eventvalidation.EventValid;
import uk.gov.hmcts.reform.bailcaseapi.infrastructure.eventvalidation.EventValidChecker;

@Slf4j
@Component
public class UploadBailSummaryEventValidChecker implements EventValidChecker<BailCase> {
    @Override
    public uk.gov.hmcts.reform.bailcaseapi.infrastructure.eventvalidation.EventValid check(Callback<BailCase> callback) {
        Event event = callback.getEvent();
        State state = callback.getCaseDetails().getState();
        if (event == Event.UPLOAD_BAIL_SUMMARY && state.equals(State.BAIL_SUMMARY_UPLOADED)) {
            final BailCase bailCase = callback.getCaseDetails().getCaseData();

            if (bailCase.read(UPLOAD_BAIL_SUMMARY_ACTION_AVAILABLE, YesOrNo.class).orElse(YesOrNo.NO) == YesOrNo.NO) {
                log.error("Bail Summary has already been uploaded to this case.");
                return new uk.gov.hmcts.reform.bailcaseapi.infrastructure.eventvalidation.EventValid("Bail Summary has already been uploaded to this case.");
            }
        }

        return new EventValid();
    }
}

