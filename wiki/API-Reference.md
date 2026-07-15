# API Reference

> The generated Javadoc is also available at: [https://hathoute.github.io/documentsearch/main/](https://hathoute.github.io/documentsearch/main/)

## Package overview

| Package | Contents |
|---|---|
| `com.hathoute.lib.openelastic` | Core `DocumentSearch` interface, `BulkEntry`, `DocumentSearchException` |
| `com.hathoute.lib.openelastic.elasticsearch` | Elasticsearch backend implementation and configuration |
| `com.hathoute.lib.openelastic.opensearch` | OpenSearch backend implementation and configuration |
| `com.hathoute.lib.openelastic.query` | Sealed query DSL hierarchy |

---

## `DocumentSearch`

`com.hathoute.lib.openelastic.DocumentSearch`

The main interface. Extends `AutoCloseable` — instances **must be closed** to release underlying network resources.

### Factory methods

```java
static DocumentSearch ofElasticSearch(ElasticSearchConfiguration configuration)
static DocumentSearch ofOpenSearch(OpenSearchConfiguration configuration)
```

### Index operations

```java
void createIndex(String index)
void deleteIndex(String index)
```

### Document operations

```java
<T> String index(String index, String id, T document)
// id can be null for auto-generation; returns the (possibly generated) id

<T> void update(String index, String id, T document)
// Partial update — only fields set on `document` are sent to the backend

void delete(String index, String id)
```

### Search

```java
<T> List<T> search(String index, Class<T> documentClass)
// Match-all search

<T> List<T> search(String index, Query query, Class<T> documentClass)
// Search using the type-safe query DSL

<T> List<T> search(String index, String jsonQuery, Class<T> documentClass)
// Raw JSON query escape hatch
// Example: search("products", "{\"match\": {\"name\": \"bike\"}}", Product.class)
```

### Bulk

```java
<T> void bulk(String index, List<BulkEntry<T>> entries)
```

### Lifecycle

```java
void close()
```

---

## `BulkEntry`

`com.hathoute.lib.openelastic.BulkEntry<T>`

A `record` holding a document id and payload for bulk indexing.

```java
static <T> BulkEntry<T> of(String id, T document)
```

The canonical constructor validates that `id` is non-blank and `document` is non-null, throwing `IllegalArgumentException` otherwise.

---

## `DocumentSearchException`

`com.hathoute.lib.openelastic.DocumentSearchException`

Unchecked `RuntimeException` thrown when any backend call fails (network, IO, transport, etc.).

```java
DocumentSearchException(String message)
DocumentSearchException(String message, Throwable cause)
```

---

## Configuration

### `ElasticSearchConfiguration`

`com.hathoute.lib.openelastic.elasticsearch.ElasticSearchConfiguration`

```java
ElasticSearchConfiguration config = ElasticSearchConfiguration.builder()
    .serverUrl("https://localhost:9200")    // required
    .apiKey("my-api-key")                    // required
    .build();
```

### `OpenSearchConfiguration`

`com.hathoute.lib.openelastic.opensearch.OpenSearchConfiguration`

```java
OpenSearchConfiguration config = OpenSearchConfiguration.builder()
    .scheme("https")                         // default: "https"
    .host("localhost")                       // required
    .port(9200)                              // default: 9200
    .username("admin")                       // optional
    .password("admin")                       // optional
    .build();
```

---

## Query DSL

`com.hathoute.lib.openelastic.query`

A sealed interface hierarchy for type-safe query construction. The root type is `Query`, which permits five implementations.

### Match all

```java
Query.matchAll()          // MatchAllQuery
```

### Full-text match

```java
Query.match("title", "hello")     // MatchQuery
```

### Term (exact, non-analyzed)

```java
Query.term("status", "published") // TermQuery
```

### Boolean compound query

```java
Query.bool()                      // returns BoolQuery.Builder
    .must(Query.match("title", "elastic"))
    .should(Query.term("tag", "search"))
    .filter(Query.term("status", "published"))
    .build()                       // returns BoolQuery
```

### Numeric range

Shortcut methods create single-bound queries:

```java
Query.gt("price", 10.0)
Query.gte("price", 10.0)
Query.lt("price", 100.0)
Query.lte("price", 100.0)
```

The builder supports multiple bounds:

```java
Query.num("price")
    .gte(10.0)
    .lt(100.0)
    .build()
```

### Date range

Shortcut methods create single-bound queries:

```java
Query.dateGt("created", "2024-01-01")
Query.dateGte("created", "2024-01-01")
Query.dateLt("created", "2024-12-31")
Query.dateLte("created", "2024-12-31")
```

The builder supports multiple bounds, format, and timezone:

```java
Query.date("created")
    .gte("2024-01-01")
    .lt("2024-12-31")
    .format("yyyy-MM-dd")
    .timeZone("UTC")
    .build()
```

### Query type hierarchy

```
Query (sealed interface)
├── MatchAllQuery  (record)
├── MatchQuery     (record, field + value)
├── TermQuery      (record, field + value)
├── BoolQuery      (final class, must/should/filter lists)
├── NumberRangeQuery (record, field + Double gt/gte/lt/lte)
└── DateRangeQuery   (record, field + String gt/gte/lt/lte + format + timeZone)
```
