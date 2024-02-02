package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isPanelRequired;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingRecordingDocument;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AutoRequestHearingService;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.PreviousRequirementsAndRequestsAppender;


@Component
@Slf4j
@RequiredArgsConstructor
public class ListCaseWithoutHearingRequirementsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final PreviousRequirementsAndRequestsAppender previousRequirementsAndRequestsAppender;
    private final FeatureToggler featureToggler;
    private final AutoRequestHearingService autoRequestHearingService;

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        final AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        // bringing the status as it would have gone the normal way and differentiating with CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS
        // this ensures all the functionality using the flags work as expected
        asylumCase.write(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.YES);
        asylumCase.write(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.YES);

        if (asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)
            && featureToggler.getValue("reheard-feature", false)) {

            previousRequirementsAndRequestsAppender.appendAndTrim(asylumCase);

            Optional<List<IdValue<HearingRecordingDocument>>> maybeHearingRecordingDocuments =
                asylumCase.read(HEARING_RECORDING_DOCUMENTS);

            final List<IdValue<HearingRecordingDocument>> hearingRecordingDocuments =
                maybeHearingRecordingDocuments.orElse(emptyList());

            asylumCase.write(PREVIOUS_HEARING_RECORDING_DOCUMENTS, hearingRecordingDocuments);
            asylumCase.write(REHEARD_CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS, YesOrNo.YES);
            asylumCase.write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.YES);
            asylumCase.clear(HAVE_HEARING_ATTENDEES_AND_DURATION_BEEN_RECORDED);
            asylumCase.clear(ATTENDING_TCW);
            asylumCase.clear(ATTENDING_JUDGE);
            asylumCase.clear(ATTENDING_APPELLANT);
            asylumCase.clear(ATTENDING_HOME_OFFICE_LEGAL_REPRESENTATIVE);
            asylumCase.clear(ATTENDING_APPELLANTS_LEGAL_REPRESENTATIVE);
            asylumCase.clear(ACTUAL_CASE_HEARING_LENGTH);
            asylumCase.clear(HEARING_CONDUCTION_OPTIONS);
            asylumCase.clear(HEARING_RECORDING_DOCUMENTS);
            asylumCase.clear(HEARING_REQUIREMENTS);
        } else {
            asylumCase.write(CASE_LISTED_WITHOUT_HEARING_REQUIREMENTS, YesOrNo.YES);
        }

        PreSubmitCallbackResponse<AsylumCase> response = new PreSubmitCallbackResponse<>(asylumCase);

        if (autoRequestHearingService.shouldAutoRequestHearing(asylumCase, !isPanelRequired(asylumCase))) {

            return new PreSubmitCallbackResponse<>(
                autoRequestHearingService.autoCreateHearing(callback));
        }

        return response;
    }
}

