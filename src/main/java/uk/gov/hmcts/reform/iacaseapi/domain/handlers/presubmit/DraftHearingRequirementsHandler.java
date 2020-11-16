package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.DocumentWithMetadata;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.WitnessDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Component
public class DraftHearingRequirementsHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;

    public DraftHearingRequirementsHandler(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && callback.getEvent() == Event.DRAFT_HEARING_REQUIREMENTS;
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

        final Optional<List<IdValue<WitnessDetails>>> mayBeWitnessDetails = asylumCase.read(WITNESS_DETAILS);

        final List<WitnessDetails> witnessDetails = mayBeWitnessDetails.orElse(Collections.emptyList()).stream().map(IdValue::getValue).collect(Collectors.toList());

        asylumCase.write(WITNESS_COUNT, witnessDetails.size());

        asylumCase.write(SUBMIT_HEARING_REQUIREMENTS_AVAILABLE, YesOrNo.YES);

        asylumCase.write(REVIEWED_HEARING_REQUIREMENTS, YesOrNo.NO);

        if (asylumCase.read(CASE_FLAG_SET_ASIDE_REHEARD_EXISTS, YesOrNo.class).map(flag -> flag.equals(YesOrNo.YES)).orElse(false)
            && featureToggler.getValue("reheard-feature", false)) {

            Optional<List<IdValue<DocumentWithMetadata>>> maybeHearingRequirements =
                asylumCase.read(HEARING_REQUIREMENTS);

            final List<IdValue<DocumentWithMetadata>> hearingRequirements =
                maybeHearingRequirements.orElse(emptyList());

            final List<IdValue<DocumentWithMetadata>> previousHearingRequirements =
                new ArrayList<>(hearingRequirements);

            asylumCase.write(PREVIOUS_HEARING_REQUIREMENTS, previousHearingRequirements);
            asylumCase.write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.NO);
            asylumCase.write(LIST_CASE_HEARING_LENGTH_VISIBLE, YesOrNo.NO);
            asylumCase.clear(MULTIMEDIA_TRIBUNAL_RESPONSE);
            asylumCase.clear(SINGLE_SEX_COURT_TRIBUNAL_RESPONSE);
            asylumCase.clear(IN_CAMERA_COURT_TRIBUNAL_RESPONSE);
            asylumCase.clear(VULNERABILITIES_TRIBUNAL_RESPONSE);
            asylumCase.clear(ADDITIONAL_TRIBUNAL_RESPONSE);
            asylumCase.clear(HEARING_REQUIREMENTS);
        } else {
            asylumCase.write(CURRENT_HEARING_DETAILS_VISIBLE, YesOrNo.YES);
        }

        return new PreSubmitCallbackResponse<>(asylumCase);
    }
}

