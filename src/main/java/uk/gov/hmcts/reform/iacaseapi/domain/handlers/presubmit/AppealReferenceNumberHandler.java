package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.*;

import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.iacaseapi.domain.DateProvider;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AppealType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.DispatchPriority;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.AppealReferenceNumberGenerator;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.Organisation;
import uk.gov.hmcts.reform.iacaseapi.infrastructure.clients.model.ccd.OrganisationPolicy;

@Service
public class AppealReferenceNumberHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private static final String DRAFT = "DRAFT";

    private final DateProvider dateProvider;

    private final AppealReferenceNumberGenerator appealReferenceNumberGenerator;

    private final FeatureToggler featureToggler;

    @Override
    public DispatchPriority getDispatchPriority() {
        return DispatchPriority.EARLIEST;
    }

    public AppealReferenceNumberHandler(
        DateProvider dateProvider,
        AppealReferenceNumberGenerator appealReferenceNumberGenerator,
        FeatureToggler featureToggler
    ) {
        this.dateProvider = dateProvider;
        this.appealReferenceNumberGenerator = appealReferenceNumberGenerator;
        this.featureToggler = featureToggler;
    }

    @Override
    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && Arrays.asList(
            Event.START_APPEAL,
            Event.SUBMIT_APPEAL,
            Event.PAY_AND_SUBMIT_APPEAL)
                   .contains(callback.getEvent());
    }

    @Override
    public PreSubmitCallbackResponse<AsylumCase> handle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback) {

        if (!canHandle(callbackStage, callback)) {
            throw new IllegalStateException("Cannot handle callback");
        }

        AsylumCase asylumCase =
            callback
                .getCaseDetails()
                .getCaseData();

        if (callback.getEvent() == Event.START_APPEAL) {

            if (featureToggler.getValue("share-case-feature", false)) {
                final OrganisationPolicy organisationPolicy = OrganisationPolicy.builder()
                    .organisation(Organisation.builder()
                        .organisationID("0UFUG4Z")
                        .build()
                    )
                    .orgPolicyCaseAssignedRole("caseworker-ia-legalrep-solicitor")
                    .orgPolicyReference("Some reference")
                    .build();

                asylumCase.write(LOCAL_AUTHORITY_POLICY, organisationPolicy);
            }

            asylumCase.write(APPEAL_REFERENCE_NUMBER, DRAFT);

            return new PreSubmitCallbackResponse<>(asylumCase);
        }

        PreSubmitCallbackResponse<AsylumCase> callbackResponse =
            new PreSubmitCallbackResponse<>(asylumCase);

        Optional<String> existingAppealReferenceNumber = asylumCase.read(APPEAL_REFERENCE_NUMBER);

        if (!existingAppealReferenceNumber.isPresent()
            || existingAppealReferenceNumber.get().equals(DRAFT)) {

            AppealType appealType =
                asylumCase
                    .read(APPEAL_TYPE, AppealType.class)
                    .orElseThrow(() -> new IllegalStateException("appealType is not present"));

            String appealReferenceNumber =
                appealReferenceNumberGenerator.generate(
                    callback.getCaseDetails().getId(),
                    appealType
                );


            asylumCase.write(APPEAL_REFERENCE_NUMBER, appealReferenceNumber);

            asylumCase.write(APPEAL_SUBMISSION_DATE, dateProvider.now().toString());
        }

        return callbackResponse;
    }
}
