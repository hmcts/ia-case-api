package uk.gov.hmcts.reform.iacaseapi.domain.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS;
import static uk.gov.hmcts.reform.iacaseapi.domain.entities.AsylumCaseFieldDefinition.HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY;
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
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.HomeOfficeAppellant;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.callback.Callback;
import uk.gov.hmcts.reform.iacaseapi.domain.entities.ccd.CaseDetails;
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

        Mockito.when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.empty());
    }

    @Test
    void should_return_existing_appellants_without_calling_api() throws Exception {

        HomeOfficeAppellant hoAppellant = new HomeOfficeAppellant();
        hoAppellant.setFamilyName("Smith");

        IdValue<HomeOfficeAppellant> idValue =
            new IdValue<>("1", hoAppellant);

        List<IdValue<HomeOfficeAppellant>> expected =
            Collections.singletonList(idValue);

        String json =
            "[{\"id\":\"1\",\"value\":{\"familyName\":\"Smith\"}}]";

        Mockito.when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.of(json));

        List<IdValue<HomeOfficeAppellant>> result =
            service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(expected, result);

        Mockito.verify(homeOfficeApi, Mockito.never())
            .midEvent(Mockito.any());
    }

    @Test
    void should_call_api_and_store_data_when_status_ok() {

        Mockito.when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.empty());

        Mockito.when(homeOfficeApi.midEvent(callback))
            .thenReturn(asylumCaseWithApiData);

        Mockito.when(asylumCaseWithApiData.read(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.OK));

        Mockito.when(asylumCaseWithApiData.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.of(appellants));

        List<IdValue<HomeOfficeAppellant>> result =
            service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        Assertions.assertFalse(result.isEmpty());
        Assertions.assertEquals(appellants, result);

        Mockito.verify(asylumCase).write(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.OK);

        Mockito.verify(asylumCase).write(
            Mockito.eq(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY),
            Mockito.any(String.class));
    }

    @Test
    void should_handle_not_found_status() {

        Mockito.when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.empty());

        Mockito.when(homeOfficeApi.midEvent(callback))
            .thenReturn(asylumCaseWithApiData);

        Mockito.when(asylumCaseWithApiData.read(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.NOT_FOUND));

        List<IdValue<HomeOfficeAppellant>> result =
            service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        Assertions.assertTrue(result.isEmpty());

        Mockito.verify(asylumCase).write(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.NOT_FOUND);
    }

    @Test
    void should_handle_server_error_status() {

        Mockito.when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.empty());

        Mockito.when(homeOfficeApi.midEvent(callback))
            .thenReturn(asylumCaseWithApiData);

        Mockito.when(asylumCaseWithApiData.read(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.INTERNAL_SERVER_ERROR));

        List<IdValue<HomeOfficeAppellant>> result =
            service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        Assertions.assertTrue(result.isEmpty());

        Mockito.verify(asylumCase).write(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.INTERNAL_SERVER_ERROR);
    }

    @Test
    void should_handle_client_error_status() {

        Mockito.when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.empty());

        Mockito.when(homeOfficeApi.midEvent(callback))
            .thenReturn(asylumCaseWithApiData);

        Mockito.when(asylumCaseWithApiData.read(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(HomeOfficeApiResponseStatusType.BAD_REQUEST));

        List<IdValue<HomeOfficeAppellant>> result =
            service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        Assertions.assertTrue(result.isEmpty());

        Mockito.verify(asylumCase).write(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.BAD_REQUEST);
    }

    @Test
    void should_use_unknown_when_status_missing() {

        Mockito.when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.empty());

        Mockito.when(homeOfficeApi.midEvent(callback))
            .thenReturn(asylumCaseWithApiData);

        Mockito.when(asylumCaseWithApiData.read(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.empty());

        List<IdValue<HomeOfficeAppellant>> result =
            service.getHomeOfficeReferenceData(HO_REFERENCE, callback);

        Assertions.assertTrue(result.isEmpty());

        Mockito.verify(asylumCase).write(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.UNKNOWN);
    }

    @Test
    void should_handle_deserialisation_exception_and_call_api() {

        when(callback.getCaseDetails())
            .thenReturn(caseDetails);

        when(caseDetails.getCaseData())
            .thenReturn(asylumCase);

        when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.of("NOT VALID JSON"));

        when(homeOfficeApi.midEvent(callback))
            .thenReturn(asylumCaseWithApiData);

        when(asylumCaseWithApiData.read(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(
                HomeOfficeApiResponseStatusType.NOT_FOUND));

        List<IdValue<HomeOfficeAppellant>> result =
            service.getHomeOfficeReferenceData(
                HO_REFERENCE,
                callback
            );

        Assertions.assertTrue(result.isEmpty());

        verify(homeOfficeApi)
            .midEvent(callback);
    }

    private static class SelfReferencingAppellant
        extends HomeOfficeAppellant {

        private SelfReferencingAppellant self;

        public SelfReferencingAppellant() {
            this.self = this;
        }

        public SelfReferencingAppellant getSelf() {
            return self;
        }
    }

    @Test
    void should_handle_serialisation_exception() {

        when(callback.getCaseDetails())
            .thenReturn(caseDetails);

        when(caseDetails.getCaseData())
            .thenReturn(asylumCase);

        when(asylumCase.read(
            HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY,
            String.class))
            .thenReturn(Optional.empty());

        when(homeOfficeApi.midEvent(callback))
            .thenReturn(asylumCaseWithApiData);

        when(asylumCaseWithApiData.read(
            HOME_OFFICE_APPELLANT_API_RESPONSE_STATUS,
            HomeOfficeApiResponseStatusType.class))
            .thenReturn(Optional.of(
                HomeOfficeApiResponseStatusType.OK));

        List<IdValue<HomeOfficeAppellant>> appellants =
            List.of(
                new IdValue<>(
                    "1",
                    new SelfReferencingAppellant()
                )
            );

        when(asylumCaseWithApiData.read(HOME_OFFICE_APPELLANTS))
            .thenReturn(Optional.of(appellants));

        List<IdValue<HomeOfficeAppellant>> result =
            service.getHomeOfficeReferenceData(
                HO_REFERENCE,
                callback
            );

        Assertions.assertEquals(appellants, result);

        verify(asylumCase, never())
            .write(
                eq(HOME_OFFICE_APPELLANTS_SERIALISED_INTERNAL_USE_ONLY),
                any(String.class)
            );
    }

}