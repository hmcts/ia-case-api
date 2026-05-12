package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCase;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.HomeOfficeApiResponseStatusType;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.field.IdValue;

@ExtendWith(MockitoExtension.class)
class HomeOfficeReferenceServiceTest {

    private static final String HO_REFERENCE = "HO123456";

    @Mock
    private HomeOfficeApi<AsylumCase> homeOfficeApi;

    @Mock
    private Callback<AsylumCase> callback;

    @Mock
    private CaseDetails<AsylumCase> caseDetails;

    @Mock
    private AsylumCase asylumCase;

    @Mock
    private AsylumCase asylumCaseWithApiData;

    @Mock
    private IdValue<HomeOfficeAppellant> appellant;

    @InjectMocks
    private HomeOfficeReferenceService service;

    private List<IdValue<HomeOfficeAppellant>> appellants;

    @BeforeEach
    void setup() {
        appellants = Collections.singletonList(appellant);

        Mockito.when(callback.getCaseDetails()).thenReturn(caseDetails);
        Mockito.when(caseDetails.getCaseData()).thenReturn(asylumCase);
    }

    @Test
    void should_return_existing_appellants_without_calling_api() {

        Mockito.when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.of(appellants));

        Optional<List<IdValue<HomeOfficeAppellant>>> result =
            service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(appellants, result.get());

        Mockito.verify(homeOfficeApi, Mockito.never()).midEvent(Mockito.any());
    }

    @Test
    void should_call_api_and_store_data_when_status_ok() {

        Mockito.when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty());

        Mockito.when(homeOfficeApi.midEvent(callback))
            .thenReturn(asylumCaseWithApiData);

        Mockito.when(asylumCaseWithApiData.read(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.OK));

        Mockito.when(asylumCaseWithApiData.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.of(appellants));

        Optional<List<IdValue<HomeOfficeAppellant>>> result =
            service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        Assertions.assertTrue(result.isPresent());
        Assertions.assertEquals(appellants, result.get());

        Mockito.verify(asylumCase).write(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.OK);

        Mockito.verify(asylumCase).write(
            HOME_OFFICE_APPELLANTS,
            Optional.of(appellants));
    }

    @Test
    void should_handle_not_found_status() {

        Mockito.when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty());

        Mockito.when(homeOfficeApi.midEvent(callback))
            .thenReturn(asylumCaseWithApiData);

        Mockito.when(asylumCaseWithApiData.read(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.NOT_FOUND));

        Optional<List<IdValue<HomeOfficeAppellant>>> result =
            service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        Assertions.assertFalse(result.isPresent());

        Mockito.verify(asylumCase).write(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.NOT_FOUND);
    }

    @Test
    void should_handle_server_error_status() {

        Mockito.when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty());

        Mockito.when(homeOfficeApi.midEvent(callback))
            .thenReturn(asylumCaseWithApiData);

        Mockito.when(asylumCaseWithApiData.read(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.INTERNAL_SERVER_ERROR));

        Optional<List<IdValue<HomeOfficeAppellant>>> result =
            service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        Assertions.assertFalse(result.isPresent());

        Mockito.verify(asylumCase).write(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.INTERNAL_SERVER_ERROR);
    }

    @Test
    void should_handle_client_error_status() {

        Mockito.when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty());

        Mockito.when(homeOfficeApi.midEvent(callback))
            .thenReturn(asylumCaseWithApiData);

        Mockito.when(asylumCaseWithApiData.read(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.BAD_REQUEST));

        Optional<List<IdValue<HomeOfficeAppellant>>> result =
            service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        Assertions.assertFalse(result.isPresent());

        Mockito.verify(asylumCase).write(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.BAD_REQUEST);
    }

    @Test
    void should_use_unknown_when_status_missing() {

        Mockito.when(asylumCase.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.empty());

        Mockito.when(homeOfficeApi.midEvent(callback))
            .thenReturn(asylumCaseWithApiData);

        Mockito.when(asylumCaseWithApiData.read(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.empty());

        Optional<List<IdValue<HomeOfficeAppellant>>> result =
            service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        Assertions.assertFalse(result.isPresent());

        Mockito.verify(asylumCase).write(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.UNKNOWN);
    }
}