package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_TYPE;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.AUTO_HEARING_REQUEST_ENABLED;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HEARING_LOCATION;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LISTING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.LIST_CASE_HEARING_LENGTH;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;
import static uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils.isIntegrated;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HearingLength;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HoursMinutes;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationBasedFeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.LocationRefDataService;

@Component
@RequiredArgsConstructor
public class ListCaseWithoutHearingRequirementsPreparer implements PreSubmitCallbackHandler<AsylumCase> {

    private final LocationBasedFeatureToggler locationBasedFeatureToggler;
    private final LocationRefDataService locationRefDataService;

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_START
               && callback.getEvent() == Event.LIST_CASE_WITHOUT_HEARING_REQUIREMENTS;
    }

    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase = callback.getCaseDetails().getCaseData();

        if (locationBasedFeatureToggler.isAutoHearingRequestEnabled(asylumCase) == YES) {
            asylumCase.write(AUTO_HEARING_REQUEST_ENABLED, YES);
            asylumCase.write(HEARING_LOCATION, locationRefDataService.getHearingLocationsDynamicList());
        } else {
            asylumCase.write(AUTO_HEARING_REQUEST_ENABLED, NO);
        }

        setDefaultHearingLengthForAppealType(asylumCase);

        HandlerUtils.setDefaultAutoListHearingValue(asylumCase);

        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    static void setDefaultHearingLengthForAppealType(AsylumCase asylumCase) {
        final Optional<AppealType> optionalAppealType = asylumCase.read(APPEAL_TYPE, AppealType.class);

        if (optionalAppealType.isPresent()) {
            AppealType appealType = optionalAppealType.get();

            switch (appealType) {
                case HU:
                case EA:
                case EU:
                    if (!isIntegrated(asylumCase)) {
                        asylumCase.write(LIST_CASE_HEARING_LENGTH, HearingLength.LENGTH_2_HOURS.toString());
                    } else {
                        asylumCase.write(LISTING_LENGTH, new HoursMinutes(2, 0));
                    }
                    break;
                case DC:
                case PA:
                case RP:
                    if (!isIntegrated(asylumCase)) {
                        asylumCase.write(LIST_CASE_HEARING_LENGTH, HearingLength.LENGTH_3_HOURS.toString());
                    } else {
                        asylumCase.write(LISTING_LENGTH, new HoursMinutes(3, 0));
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
