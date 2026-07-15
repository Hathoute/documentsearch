# open-elastic

A lightweight Java library that provides a unified abstraction over Elasticsearch and OpenSearch document-search backends.

## Overview

`open-elastic` wraps the official Elasticsearch Java client and the official OpenSearch Java client behind a single, consistent interface (`DocumentSearch`). Write your code once against the abstraction and choose which backend to use at construction time.

Both backends are bundled by default — no extra dependencies required.

## Usage

### Maven

```xml
<dependency>
    <groupId>com.hathoute.lib</groupId>
    <artifactId>open-elastic</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### Quick start

```java
// Elasticsearch
DocumentSearch search = DocumentSearch.ofElasticSearch(
    ElasticSearchConfiguration.builder()
        .serverUrl("https://localhost:9200")
        .apiKey("my-api-key")
        .build());

// OpenSearch
DocumentSearch search = DocumentSearch.ofOpenSearch(
    OpenSearchConfiguration.builder()
        .scheme("https")
        .host("localhost")
        .port(9200)
        .username("admin")
        .password("admin")
        .skipSslVerification(true)
        .build());

// Create an index
search.createIndex("my-index");

// Index a document
String id = search.index("my-index", "doc-1", new MyDocument("hello"));

// Search (match_all)
List<MyDocument> results = search.search("my-index", MyDocument.class);

// Search with query DSL
List<MyDocument> results = search.search("my-index",
    Query.match("title", "hello"), MyDocument.class);

// Search with raw JSON
List<MyDocument> results = search.search("my-index",
    "{\"match\": {\"title\": \"hello\"}}", MyDocument.class);

// Update
search.update("my-index", "doc-1", new MyDocument("world"));

// Delete
search.delete("my-index", "doc-1");

// Bulk index
List<BulkEntry<MyDocument>> entries = List.of(
    BulkEntry.of("1", new MyDocument("a")),
    BulkEntry.of("2", new MyDocument("b")));
search.bulk("my-index", entries);

search.close();
```

## Query DSL

A sealed interface hierarchy for type-safe query construction:

```java
// Match all
Query.matchAll()

// Full-text match
Query.match("fieldName", value)

// Term (exact match, non-analyzed)
Query.term("fieldName", value)

// Boolean compound query
Query.bool()
    .must(Query.match("title", "elastic"))
    .filter(Query.term("status", "published"))
    .build()
```

## Build

```bash
# Build and run tests
mvn clean package

# Skip integration tests (no Docker required)
mvn package -DskipTests
```

Integration tests use Testcontainers and require Docker.

## Requirements

- Java 17+
- Apache Maven 3.x
- Docker (for integration tests)

## License

Apache 2.0
