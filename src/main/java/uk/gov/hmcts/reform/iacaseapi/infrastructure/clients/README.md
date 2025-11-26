# CCD Elasticsearch Integration for Appeal Reference Number Validation

## Overview

This package contains a pluggable Elasticsearch service that searches CCD case data to validate whether an appeal reference number already exists in the system. The implementation follows a clean architecture pattern with separated concerns for querying, repository access, and business logic.

## Architecture

The implementation consists of several layers:

### 1. Query Builder (`CcdElasticSearchQueryBuilder`)
- **Purpose**: Constructs Elasticsearch queries in the format expected by CCD
- **Responsibility**: Encapsulates query DSL construction logic
- **Key Method**: `buildAppealReferenceNumberQuery(String appealReferenceNumber)`

### 2. Repository Layer (`CcdElasticSearchRepository`)
- **Purpose**: Handles low-level HTTP communication with CCD's Elasticsearch API
- **Responsibility**: Manages authentication, REST calls, and error handling
- **Dependencies**: `RestTemplate`, `AuthTokenGenerator`, `UserDetails`

### 3. Service Layer (`AppealReferenceNumberSearchService`)
- **Purpose**: Provides high-level API for checking appeal reference number existence
- **Responsibility**: Orchestrates query building and repository calls, handles business logic
- **Key Method**: `appealReferenceNumberExists(String appealReferenceNumber)`

### 4. Domain Models
- `CcdSearchQuery`: Represents an Elasticsearch query structure
- `CcdSearchResult`: Represents search response from CCD
- `CcdCase`: Represents individual case in search results

## Integration with Validator

The `AppealReferenceNumberValidator` has been enhanced to:
1. First check the database (existing behavior)
2. Then check CCD via Elasticsearch (new behavior)

This provides comprehensive validation across both data sources.

## Configuration

The service requires the following configuration in `application.yaml`:

```yaml
core_case_data_api_url: ${CCD_URL:http://127.0.0.1:4452}
```

The service automatically uses:
- **Case Type**: `Asylum`
- **Jurisdiction**: `IA`
- **Search Endpoint**: `/searchCases?ctid=Asylum`

## Usage Example

```java
@Autowired
private AppealReferenceNumberSearchService searchService;

public void validateAppeal(String appealReferenceNumber) {
    boolean exists = searchService.appealReferenceNumberExists(appealReferenceNumber);
    if (exists) {
        throw new ValidationException("Appeal reference number already exists");
    }
}
```

## Error Handling

The service implements graceful error handling:
- Returns `false` if search fails (fail-open approach)
- Logs all errors for monitoring
- Throws `CcdSearchException` from repository layer for specific error handling

## Security

The service automatically handles:
- User authentication via `UserDetails.getAccessToken()`
- Service-to-service authentication via `AuthTokenGenerator.generate()`
- Proper HTTP headers for CCD API access

## Testing

Comprehensive unit tests are provided for all components:
- `CcdElasticSearchQueryBuilderTest`: Tests query construction
- `CcdElasticSearchRepositoryTest`: Tests repository operations with mocked HTTP calls
- `AppealReferenceNumberSearchServiceTest`: Tests service orchestration and error handling
- `AppealReferenceNumberValidatorTest`: Tests integrated validation logic

## Dependencies

The implementation uses:
- `core-case-data-store-client:4.9.2` (CCD client library)
- Spring `RestTemplate` for HTTP communication
- Service Auth Provider for S2S authentication

## Performance Considerations

- Default search size: 10 results (only need to know if any exist)
- Only fetches minimal fields: `reference`, `data.appealReferenceNumber`, `id`
- Caching can be added at the service layer if needed

## Extensibility

The design is pluggable and can be extended:
- Add new query types by extending `CcdElasticSearchQueryBuilder`
- Support other case fields by modifying query structure
- Add retry logic in repository layer
- Implement caching in service layer

