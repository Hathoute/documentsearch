# open-elastic

A lightweight Java library that provides a unified abstraction over Elasticsearch and OpenSearch document-search backends. Write your search/indexing code once against a single `DocumentSearch` interface and choose which backend to use at construction time.

Both backends are bundled by default — no extra dependencies required.

## Getting Started

- [[Installation]] — Add open-elastic to your Maven project
- [[API Reference]] — Full API documentation (or [browse the Javadoc](https://<owner>.github.io/documentsearch/))
- [[Javadoc]] — Generated Javadoc HTML
- [[Version Overrides]] — How to override bundled dependency versions using Maven excludes

## Requirements

- Java 17+
- Apache Maven 3.x
- Docker (only for integration tests)

## Build

```bash
mvn clean package
# Skip integration tests (no Docker required):
mvn package -DskipTests
```

## License

Apache 2.0
