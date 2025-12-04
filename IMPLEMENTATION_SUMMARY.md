# Implementation Summary: Appeal Reference Number Elasticsearch Validation

## Overview
This implementation adds a pluggable Elasticsearch service that searches CCD case data to validate whether an appeal reference number already exists. The service integrates with the existing `AppealReferenceNumberValidator` to provide comprehensive validation across both the database and CCD.

## Changes Made

### 1. Dependencies (`build.gradle`)
- **Added**: `core-case-data-store-client:4.9.2` to main implementation scope
- This provides the necessary CCD client libraries for Elasticsearch integration

### 2. New Infrastructure Components

#### Query Builder
**File**: `src/main/java/uk/gov/hmcts/reform/iacaseapi/infrastructure/clients/CcdElasticSearchQueryBuilder.java`
- Builds Elasticsearch queries for CCD case searches
- Constructs queries with appropriate filters for case type (`Asylum`) and jurisdiction (`IA`)
- Returns structured query objects that match CCD's Elasticsearch API format

#### Repository
**File**: `src/main/java/uk/gov/hmcts/reform/iacaseapi/infrastructure/clients/CcdElasticSearchRepository.java`
- Handles low-level HTTP communication with CCD's Elasticsearch API
- Manages authentication (user token + S2S token)
- Provides exception handling with custom `CcdSearchException`
- Uses Spring `RestTemplate` for HTTP operations

#### Domain Models
**Files**:
- `src/main/java/uk/gov/hmcts/reform/iacaseapi/infrastructure/clients/CcdSearchQuery.java`
  - Represents an Elasticsearch query structure
- `src/main/java/uk/gov/hmcts/reform/iacaseapi/infrastructure/clients/model/ccd/CcdSearchResult.java`
  - Represents the search response from CCD
- `src/main/java/uk/gov/hmcts/reform/iacaseapi/infrastructure/clients/model/ccd/CcdCase.java`
  - Represents individual case data in search results

### 3. Service Layer

#### Search Service
**File**: `src/main/java/uk/gov/hmcts/reform/iacaseapi/domain/service/AppealReferenceNumberSearchService.java`
- High-level service for checking appeal reference number existence
- Orchestrates query building and repository calls
- Implements graceful error handling (returns `false` on search failure)
- Provides comprehensive logging for monitoring

### 4. Updated Validator

#### AppealReferenceNumberValidator
**File**: `src/main/java/uk/gov/hmcts/reform/iacaseapi/domain/service/AppealReferenceNumberValidator.java`
- **Enhanced validation flow**:
  1. Validates format (existing)
  2. Checks database existence (existing)
  3. **NEW**: Checks CCD existence via Elasticsearch
- Maintains backward compatibility
- Fail-fast approach: stops at first error found

### 5. Comprehensive Test Suite

#### Unit Tests Created:
1. **CcdElasticSearchQueryBuilderTest**
   - Tests query construction
   - Validates bool query structure
   - Verifies correct field selection

2. **CcdElasticSearchRepositoryTest**
   - Tests successful search execution
   - Tests error handling
   - Verifies correct header usage

3. **AppealReferenceNumberSearchServiceTest**
   - Tests existence detection (positive/negative cases)
   - Tests null/empty input handling
   - Tests exception handling
   - Tests edge cases (multiple results, null results)

4. **AppealReferenceNumberValidatorTest**
   - Tests integrated validation flow
   - Tests all valid appeal type prefixes
   - Tests format validation
   - Tests database-first checking strategy
   - Tests CCD checking as secondary validation

## Architecture Principles

### Separation of Concerns
- **Query Builder**: Constructs queries
- **Repository**: Handles HTTP/REST communication
- **Service**: Implements business logic
- **Validator**: Orchestrates validation flow

### Pluggability
- All components are Spring-managed beans
- Dependencies injected via constructor
- Easy to mock for testing
- Easy to swap implementations

### Error Handling
- Repository throws specific exceptions
- Service catches and logs errors
- Validation continues on search failure (fail-open)
- Comprehensive logging at all levels

### Security
- Automatic user authentication via `UserDetails`
- Automatic S2S authentication via `AuthTokenGenerator`
- Proper HTTP headers set for CCD API

## Configuration Requirements

The service uses existing configuration from `application.yaml`:
```yaml
core_case_data_api_url: ${CCD_URL:http://127.0.0.1:4452}
```

No additional configuration needed.

## Testing Strategy

### Unit Tests
- All components have comprehensive unit tests
- Mockito used for dependency mocking
- Edge cases covered
- Error scenarios tested

### Integration Points
- Repository can be integration tested against real CCD (functional tests)
- Service can be tested with stubbed repository
- Validator can be tested end-to-end

## Performance Considerations

- **Minimal data fetched**: Only `reference`, `data.appealReferenceNumber`, `id`
- **Limited result size**: Default 10 results (only need to know if any exist)
- **Fail-fast validation**: Stops at first error
- **Database checked first**: Faster local check before remote API call

## Monitoring & Logging

All components provide structured logging:
- Info level: Search attempts and results
- Warn level: Null/invalid inputs
- Error level: Search failures

Log messages include:
- Appeal reference number (for tracing)
- Result counts (for monitoring)
- Error details (for debugging)

## Future Enhancements

Possible extensions:
1. **Caching**: Add cache at service layer to reduce API calls
2. **Retry Logic**: Add retry mechanism for transient failures
3. **Metrics**: Add performance metrics collection
4. **Batch Validation**: Support validating multiple reference numbers
5. **Additional Queries**: Extend query builder for other search scenarios

## Files Changed

### New Files (8):
1. `src/main/java/uk/gov/hmcts/reform/iacaseapi/infrastructure/clients/CcdSearchQuery.java`
2. `src/main/java/uk/gov/hmcts/reform/iacaseapi/infrastructure/clients/CcdElasticSearchQueryBuilder.java`
3. `src/main/java/uk/gov/hmcts/reform/iacaseapi/infrastructure/clients/CcdElasticSearchRepository.java`
4. `src/main/java/uk/gov/hmcts/reform/iacaseapi/infrastructure/clients/model/ccd/CcdSearchResult.java`
5. `src/main/java/uk/gov/hmcts/reform/iacaseapi/infrastructure/clients/model/ccd/CcdCase.java`
6. `src/main/java/uk/gov/hmcts/reform/iacaseapi/domain/service/AppealReferenceNumberSearchService.java`
7. `src/main/java/uk/gov/hmcts/reform/iacaseapi/infrastructure/clients/README.md`
8. `IMPLEMENTATION_SUMMARY.md` (this file)

### Test Files (4):
1. `src/test/java/uk/gov/hmcts/reform/iacaseapi/infrastructure/clients/CcdElasticSearchQueryBuilderTest.java`
2. `src/test/java/uk/gov/hmcts/reform/iacaseapi/infrastructure/clients/CcdElasticSearchRepositoryTest.java`
3. `src/test/java/uk/gov/hmcts/reform/iacaseapi/domain/service/AppealReferenceNumberSearchServiceTest.java`
4. `src/test/java/uk/gov/hmcts/reform/iacaseapi/domain/service/AppealReferenceNumberValidatorTest.java`

### Modified Files (2):
1. `build.gradle` - Added dependency
2. `src/main/java/uk/gov/hmcts/reform/iacaseapi/domain/service/AppealReferenceNumberValidator.java` - Enhanced validation

## Testing the Implementation

### Run Unit Tests
```bash
./gradlew test --tests "*CcdElasticSearch*"
./gradlew test --tests "*AppealReferenceNumberSearch*"
./gradlew test --tests "AppealReferenceNumberValidatorTest"
```

### Run All Tests
```bash
./gradlew test
```

### Run with Coverage
```bash
./gradlew test jacocoTestReport
```

## Deployment Considerations

1. **No database migrations needed**
2. **No configuration changes required** (uses existing CCD URL)
3. **Backward compatible** (existing validation still works)
4. **Feature toggle ready** (can be disabled by injecting no-op implementation)
5. **No changes to CCD** (read-only operations)

## Summary

This implementation provides a robust, pluggable, and well-tested Elasticsearch integration for appeal reference number validation. The architecture follows SOLID principles, is easy to maintain, and can be extended for future requirements. All components are thoroughly tested and documented.

