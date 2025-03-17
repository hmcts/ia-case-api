package uk.gov.hmcts.reform.iacaseapi.domain.handlers.presubmit;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPEAL_OUT_OF_COUNTRY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.APPELLANT_IN_UK;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HAS_SPONSOR;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_ADDRESS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_ADDRESS_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_FAMILY_NAME;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_GIVEN_NAMES;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.SPONSOR_NAME_FOR_DISPLAY;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.NO;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo.YES;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.Event;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackResponse;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.PreSubmitCallbackStage;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.AddressUk;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.YesOrNo;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.HandlerUtils;
import uk.gov.hmcts.reform.iacaseapi.domain.handlers.PreSubmitCallbackHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.service.FeatureToggler;

@Slf4j
@Component
public class AppealOutOfCountryHandler implements PreSubmitCallbackHandler<AsylumCase> {

    private final FeatureToggler featureToggler;

    public AppealOutOfCountryHandler(FeatureToggler featureToggler) {
        this.featureToggler = featureToggler;
    }

    public boolean canHandle(
        PreSubmitCallbackStage callbackStage,
        Callback<AsylumCase> callback
    ) {
        requireNonNull(callbackStage, "callbackStage must not be null");
        requireNonNull(callback, "callback must not be null");

        return callbackStage == PreSubmitCallbackStage.ABOUT_TO_SUBMIT
               && Arrays.asList(
            Event.START_APPEAL,
            Event.EDIT_APPEAL,
            Event.EDIT_APPEAL_AFTER_SUBMIT).contains(callback.getEvent());
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

        boolean isAipJourney = HandlerUtils.isAipJourney(asylumCase);

        //Default consider appellant living in UK
        AtomicReference<YesOrNo> outOfCountry = new AtomicReference<>(NO);
        asylumCase.read(APPELLANT_IN_UK, YesOrNo.class).ifPresent(
            appellantInUk -> outOfCountry.set(appellantInUk.equals(NO) ? YES : NO)
        );
        asylumCase.write(APPEAL_OUT_OF_COUNTRY, outOfCountry.get());
        asylumCase.write(APPELLANT_IN_UK, outOfCountry.get().equals(YES) ? YES : NO);

        final long caseId = callback.getCaseDetails().getId();
        log.info("Appeal Out Of Country: {} for case ID {}", outOfCountry.get(), caseId);

        Optional<YesOrNo> hasSponsor = asylumCase.read(HAS_SPONSOR, YesOrNo.class);
        if (hasSponsor.isPresent() && hasSponsor.get().equals(YES)) {
            log.info("Sponsor present for Out Of Country appeal. case ID {}", caseId);

            String sponsorNameForDisplay = null;

            if (!isAipJourney) {
                final String sponsorGivenNames =
                    asylumCase
                        .read(SPONSOR_GIVEN_NAMES, String.class)
                        .orElseThrow(() -> new IllegalStateException("sponsorGivenNames is not present"));

                final String sponsorFamilyName =
                    asylumCase
                        .read(SPONSOR_FAMILY_NAME, String.class)
                        .orElseThrow(() -> new IllegalStateException("sponsorFamilyName is not present"));

                sponsorNameForDisplay = sponsorGivenNames + " " + sponsorFamilyName;
                writeSponsorNameForDisplay(asylumCase, sponsorNameForDisplay);
            }

            if (isAipJourney && featureToggler.getValue("aip-ooc-feature", false)) {
                Optional<String> sponsorGivenNames = asylumCase.read(SPONSOR_GIVEN_NAMES, String.class);
                Optional<String> sponsorFamilyName = asylumCase.read(SPONSOR_FAMILY_NAME, String.class);

                if (sponsorGivenNames.isPresent() && sponsorFamilyName.isPresent()) {
                    sponsorNameForDisplay = sponsorGivenNames.get() + " " + sponsorFamilyName.get();
                    writeSponsorNameForDisplay(asylumCase, sponsorNameForDisplay);
                }
            }

            Optional<AddressUk> optionalAddressUk = asylumCase.read(SPONSOR_ADDRESS, AddressUk.class);
            if (optionalAddressUk.isPresent()) {
                AddressUk addressUk = optionalAddressUk.get();
                StringBuilder sponsorAddress = new StringBuilder("");
                addAddressLine(addressUk.getAddressLine1(), sponsorAddress);
                addAddressLine(addressUk.getAddressLine2(), sponsorAddress);
                addAddressLine(addressUk.getAddressLine3(), sponsorAddress);
                addAddressLine(addressUk.getPostTown(), sponsorAddress);
                addAddressLine(addressUk.getCounty(), sponsorAddress);
                addAddressLine(addressUk.getPostCode(), sponsorAddress);
                addressUk.getCountry().ifPresent(
                    line -> {
                        if (!line.equals("")) {
                            sponsorAddress.append(line);
                        }
                    }
                );
                asylumCase.write(SPONSOR_ADDRESS_FOR_DISPLAY, sponsorAddress.toString());
            }
        }
        return new PreSubmitCallbackResponse<>(asylumCase);
    }

    private void writeSponsorNameForDisplay(AsylumCase asylumCase, String sponsorNameForDisplay) {
        asylumCase.write(
            SPONSOR_NAME_FOR_DISPLAY,
            sponsorNameForDisplay.replaceAll("\\s+", " ").trim()
        );
    }

    private void addAddressLine(final Optional<String> addressLine, StringBuilder sponsorDetails) {
        addressLine.ifPresent(
            line -> {
                if (!line.equals("")) {
                    sponsorDetails.append(line).append("\r\n");
                }
            }
        );
    }
}
