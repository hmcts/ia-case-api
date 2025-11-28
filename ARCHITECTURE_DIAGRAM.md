# Architecture Diagram: Appeal Reference Number Elasticsearch Validation

## Component Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                   AppealReferenceNumberValidator                     │
│                      (Domain Service Layer)                          │
│                                                                       │
│  ┌────────────────────────────────────────────────────────────────┐ │
│  │ Validation Flow:                                                │ │
│  │ 1. Format Validation (Regex)                                   │ │
│  │ 2. Database Check (AppealReferenceNumberGenerator)             │ │
│  │ 3. CCD Elasticsearch Check (AppealReferenceNumberSearchService)│ │
│  └────────────────────────────────────────────────────────────────┘ │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ uses
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│              AppealReferenceNumberSearchService                      │
│                      (Service Layer)                                 │
│                                                                       │
│  • Orchestrates search operation                                     │
│  • Handles null/empty inputs                                         │
│  • Graceful error handling (fail-open)                               │
│  • Comprehensive logging                                             │
└───────────────┬───────────────────────────────┬─────────────────────┘
                │ uses                           │ uses
                ▼                                ▼
┌───────────────────────────────┐  ┌────────────────────────────────┐
│ CcdElasticSearchQueryBuilder  │  │  CcdElasticSearchRepository    │
│   (Query Construction)         │  │    (HTTP/REST Layer)           │
│                                │  │                                │
│ • Builds Elasticsearch queries │  │ • HTTP communication with CCD  │
│ • Encapsulates query DSL       │  │ • Authentication (User + S2S)  │
│ • Case type & jurisdiction     │  │ • Exception handling           │
│   filtering                    │  │ • RestTemplate operations      │
└───────────────────────────────┘  └────────────┬───────────────────┘
                                                 │ HTTP POST
                                                 ▼
                                    ┌────────────────────────────────┐
                                    │    CCD Elasticsearch API       │
                                    │  /searchCases?ctid=Asylum      │
                                    │                                │
                                    │  • Searches case_data index    │
                                    │  • Returns matching cases      │
                                    └────────────────────────────────┘
```

## Data Flow

```
┌──────────────┐
│ User Request │
│ (Validate    │
│  Reference#) │
└──────┬───────┘
       │
       ▼
┌──────────────────────────────────────────────────────────────┐
│ 1. Format Check                                              │
│    Pattern: ^(HU|DA|DC|EA|PA|RP|LE|LD|LP|LH|LR|IA)/\d{5}/20\d{2}$ │
└──────┬───────────────────────────────────────────────────────┘
       │ ✓ Valid format
       ▼
┌──────────────────────────────────────────────────────────────┐
│ 2. Database Check                                            │
│    AppealReferenceNumberGenerator.referenceNumberExists()    │
└──────┬───────────────────────────────────────────────────────┘
       │ ✗ Not in DB
       ▼
┌──────────────────────────────────────────────────────────────┐
│ 3. Build Elasticsearch Query                                 │
│    CcdElasticSearchQueryBuilder.buildAppealReferenceNumberQuery() │
│                                                              │
│    {                                                         │
│      "query": {                                              │
│        "bool": {                                             │
│          "must": [                                           │
│            {"match": {"data.appealReferenceNumber": "..."}},│
│            {"match": {"case_type_id": "Asylum"}},           │
│            {"match": {"jurisdiction": "IA"}}                │
│          ]                                                   │
│        }                                                     │
│      },                                                      │
│      "size": 10,                                             │
│      "_source": ["reference", "data.appealReferenceNumber", "id"] │
│    }                                                         │
└──────┬───────────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────────┐
│ 4. Execute Search                                            │
│    CcdElasticSearchRepository.searchCases()                  │
│                                                              │
│    POST http://ccd-api/searchCases?ctid=Asylum               │
│    Headers:                                                  │
│      Authorization: Bearer <user-token>                      │
│      ServiceAuthorization: <s2s-token>                       │
│      Content-Type: application/json                          │
└──────┬───────────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────────┐
│ 5. Process Response                                          │
│    CcdSearchResult {                                         │
│      total: <count>,                                         │
│      cases: [...]                                            │
│    }                                                         │
└──────┬───────────────────────────────────────────────────────┘
       │
       ▼
┌──────────────────────────────────────────────────────────────┐
│ 6. Return Result                                             │
│    • total > 0  → Reference exists (ERROR)                   │
│    • total = 0  → Reference unique (OK)                      │
│    • exception  → Return false (fail-open)                   │
└──────────────────────────────────────────────────────────────┘
```

## Class Diagram

```
┌───────────────────────────────────────────┐
│  AppealReferenceNumberValidator           │
├───────────────────────────────────────────┤
│ - appealReferenceNumberGenerator          │
│ - appealReferenceNumberSearchService      │
├───────────────────────────────────────────┤
│ + validate(String): List<String>          │
└───────────────────┬───────────────────────┘
                    │
                    │ depends on
                    ▼
┌───────────────────────────────────────────┐
│  AppealReferenceNumberSearchService       │
├───────────────────────────────────────────┤
│ - queryBuilder: CcdElasticSearchQueryBuilder │
│ - searchRepository: CcdElasticSearchRepository│
├───────────────────────────────────────────┤
│ + appealReferenceNumberExists(String): boolean│
└─────────┬─────────────────┬───────────────┘
          │                 │
          │ uses            │ uses
          ▼                 ▼
┌────────────────────┐  ┌──────────────────────────┐
│CcdElasticSearch    │  │CcdElasticSearch          │
│QueryBuilder        │  │Repository                │
├────────────────────┤  ├──────────────────────────┤
│                    │  │- restTemplate            │
│                    │  │- serviceAuthTokenGenerator│
│                    │  │- userDetails             │
│                    │  │- ccdUrl: String          │
├────────────────────┤  ├──────────────────────────┤
│+ buildAppeal       │  │+ searchCases(            │
│  ReferenceNumber   │  │    CcdSearchQuery):      │
│  Query(): Query    │  │    CcdSearchResult       │
└────────────────────┘  └──────────────────────────┘
         │                        │
         │ produces               │ returns
         ▼                        ▼
┌────────────────────┐  ┌──────────────────────────┐
│ CcdSearchQuery     │  │ CcdSearchResult          │
├────────────────────┤  ├──────────────────────────┤
│- query: Map        │  │- total: int              │
│- size: int         │  │- cases: List<CcdCase>    │
│- source: List      │  └──────────────────────────┘
└────────────────────┘           │
                                 │ contains
                                 ▼
                        ┌──────────────────────────┐
                        │ CcdCase                  │
                        ├──────────────────────────┤
                        │- id: Long                │
                        │- reference: Long         │
                        │- data: Map<String, Object>│
                        └──────────────────────────┘
```

## Sequence Diagram

```
User          Validator         SearchService       QueryBuilder      Repository         CCD API
 │                │                    │                  │                │                 │
 │ validate(ref#) │                    │                  │                │                 │
 ├───────────────>│                    │                  │                │                 │
 │                │                    │                  │                │                 │
 │                │ 1. Format check    │                  │                │                 │
 │                │──────────┐         │                  │                │                 │
 │                │          │         │                  │                │                 │
 │                │<─────────┘         │                  │                │                 │
 │                │                    │                  │                │                 │
 │                │ 2. DB check        │                  │                │                 │
 │                │──────────┐         │                  │                │                 │
 │                │          │         │                  │                │                 │
 │                │<─────────┘         │                  │                │                 │
 │                │                    │                  │                │                 │
 │                │ 3. CCD check       │                  │                │                 │
 │                │ exists(ref#)       │                  │                │                 │
 │                ├───────────────────>│                  │                │                 │
 │                │                    │                  │                │                 │
 │                │                    │ 4. buildQuery(ref#)               │                 │
 │                │                    ├─────────────────>│                │                 │
 │                │                    │                  │                │                 │
 │                │                    │ 5. CcdSearchQuery│                │                 │
 │                │                    │<─────────────────┤                │                 │
 │                │                    │                  │                │                 │
 │                │                    │ 6. searchCases(query)             │                 │
 │                │                    ├──────────────────────────────────>│                 │
 │                │                    │                  │                │                 │
 │                │                    │                  │                │ 7. POST /search │
 │                │                    │                  │                ├────────────────>│
 │                │                    │                  │                │                 │
 │                │                    │                  │                │ 8. SearchResult │
 │                │                    │                  │                │<────────────────┤
 │                │                    │                  │                │                 │
 │                │                    │ 9. CcdSearchResult                │                 │
 │                │                    │<──────────────────────────────────┤                 │
 │                │                    │                  │                │                 │
 │                │ 10. boolean (exists)                  │                │                 │
 │                │<───────────────────┤                  │                │                 │
 │                │                    │                  │                │                 │
 │ 11. List<Error>│                    │                  │                │                 │
 │<───────────────┤                    │                  │                │                 │
 │                │                    │                  │                │                 │
```

## Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      ia-case-api                                 │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Domain Layer                                             │   │
│  │  • AppealReferenceNumberValidator                         │   │
│  │  • AppealReferenceNumberSearchService                     │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Infrastructure Layer                                     │   │
│  │  • CcdElasticSearchQueryBuilder                           │   │
│  │  • CcdElasticSearchRepository                             │   │
│  │  • RestTemplate (Spring)                                  │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                   │
└───────────────────────────┬─────────────────────────────────────┘
                            │ HTTP
                            │ Authorization: Bearer <user-token>
                            │ ServiceAuthorization: <s2s-token>
                            ▼
┌─────────────────────────────────────────────────────────────────┐
│                    CCD Data Store API                            │
│                                                                   │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  REST API Layer                                           │   │
│  │  POST /searchCases?ctid=Asylum                            │   │
│  └──────────────────────────────────────────────────────────┘   │
│                            │                                      │
│                            ▼                                      │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Elasticsearch Integration                                │   │
│  └──────────────────────────────────────────────────────────┘   │
│                            │                                      │
└────────────────────────────┼──────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Elasticsearch                               │
│                                                                   │
│  Index: asylum_cases                                             │
│  • case_type_id                                                  │
│  • jurisdiction                                                  │
│  • data.appealReferenceNumber                                    │
│  • reference                                                     │
│  • ...                                                           │
└─────────────────────────────────────────────────────────────────┘
```

## Error Handling Flow

```
┌──────────────────────────────────────────────────────────────┐
│ Search Request                                               │
└────────┬─────────────────────────────────────────────────────┘
         │
         ▼
    ┌────────┐
    │Try     │
    └────┬───┘
         │
         ├─Success────> Return CcdSearchResult
         │              (total > 0 = exists)
         │
         └─Exception──> Log Error
                        │
                        ▼
                   Return false
                   (Fail-open: don't block operation)
                        │
                        ▼
                   Continue validation
                   (May still pass if not in DB)
```

This architecture ensures:
- **Separation of Concerns**: Clear layer boundaries
- **Testability**: Easy to mock dependencies
- **Resilience**: Graceful error handling
- **Security**: Proper authentication at all levels
- **Performance**: Minimal data transfer
- **Maintainability**: Clean code structure

