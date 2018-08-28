package uk.gov.hmcts.reform.iacaseapi.domain.handlers;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Collections;
import java.util.Optional;
import org.apache.commons.lang.WordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.iacaseapi.domain.CcdEventHandler;
import uk.gov.hmcts.reform.iacaseapi.domain.datasource.HomeOfficeAsylumDataFetcher;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HomeOfficeAsylumData;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.Name;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.*;

@Component
public class HomeOfficeAsylumDataAugmentor implements CcdEventHandler<AsylumCase> {

    private static final org.slf4j.Logger LOG = getLogger(HomeOfficeAsylumDataAugmentor.class);

    private final HomeOfficeAsylumDataFetcher homeOfficeAsylumDataFetcher;

    public HomeOfficeAsylumDataAugmentor(
        @Autowired HomeOfficeAsylumDataFetcher homeOfficeAsylumDataFetcher
    ) {
        this.homeOfficeAsylumDataFetcher = homeOfficeAsylumDataFetcher;
    }

    public boolean canHandle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        return stage == Stage.ABOUT_TO_SUBMIT
               && ccdEvent.getEventId() == EventId.START_DRAFT_APPEAL;
    }

    public CcdEventResponse<AsylumCase> handle(
        Stage stage,
        CcdEvent<AsylumCase> ccdEvent
    ) {
        if (!canHandle(stage, ccdEvent)) {
            throw new IllegalStateException("Cannot handle ccd event");
        }

        AsylumCase asylumCase =
            ccdEvent
                .getCaseDetails()
                .getCaseData();

        CcdEventResponse<AsylumCase> ccdEventResponse =
            new CcdEventResponse<>(asylumCase);

        final String homeOfficeReferenceNumber = asylumCase.getHomeOfficeReferenceNumber();

        Optional<HomeOfficeAsylumData> homeOfficeAsylumData =
            homeOfficeAsylumDataFetcher.fetch(homeOfficeReferenceNumber);

        if (homeOfficeAsylumData.isPresent()) {

            LOG.info("Home Office decision found for reference number: {}", homeOfficeReferenceNumber);
            augmentCaseWithHomeOfficeData(asylumCase, homeOfficeAsylumData.get());

            if (asylumCase.getHomeOfficeDecisionDate() == null
                || !asylumCase.getHomeOfficeDecisionDate().equals(homeOfficeAsylumData.get().getDate())) {
                ccdEventResponse
                    .getErrors()
                    .add("Home Office decision date does not match the letter sent");
            }

        } else {

            LOG.info("Home Office decision *not* found for reference number: {}", homeOfficeReferenceNumber);
            ccdEventResponse
                .getErrors()
                .add("Home Office reference number '" + homeOfficeReferenceNumber + "' is not recognised");
        }

        return ccdEventResponse;
    }

    private void augmentCaseWithHomeOfficeData(
        AsylumCase asylumCase,
        HomeOfficeAsylumData homeOfficeAsylumData
    ) {
        asylumCase.setAppellantName(
            new Name(
                homeOfficeAsylumData.getTitle().orElse(null),
                homeOfficeAsylumData.getFirstName().orElse(null),
                homeOfficeAsylumData.getLastName().orElse(null)
            )
        );

        if (homeOfficeAsylumData.getDateOfBirth().isPresent()) {
            asylumCase.setAppellantDob(homeOfficeAsylumData.getDateOfBirth().get());
        }

        if (homeOfficeAsylumData.getNationality().isPresent()) {
            asylumCase.setAppellantNationalities(
                Collections.singletonList(
                    new MultiSelectValue(
                        homeOfficeAsylumData.getNationality().get(),
                        homeOfficeAsylumData.getNationality().get()
                    )
                )
            );
        }

        if (homeOfficeAsylumData.getStateless().isPresent()) {
            asylumCase.setAppellantNationalityContested(
                WordUtils.capitalize(homeOfficeAsylumData.getStateless().get())
            );
        }

        asylumCase.setAppellantAddress(
            new AddressUK(
                homeOfficeAsylumData.getAddress1().orElse(null),
                homeOfficeAsylumData.getAddress2().orElse(null),
                null,
                homeOfficeAsylumData.getAddressTown().orElse(null),
                homeOfficeAsylumData.getAddressCounty().orElse(null),
                homeOfficeAsylumData.getAddressPostcode().orElse(null),
                homeOfficeAsylumData.getAddressCountry().orElse(null)
            )
        );
    }
}
