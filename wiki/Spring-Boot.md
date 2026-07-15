# Spring Boot

## Configuration properties

Bind your backend choice and connection details via `@ConfigurationProperties`:

```java
@ConfigurationProperties(prefix = "documentsearch")
public record DocumentSearchProperties(
    Type type,
    Elasticsearch elasticsearch,
    Opensearch opensearch
) {
    public enum Type { elasticsearch, opensearch }

    public record Elasticsearch(String serverUrl, String apiKey) {}

    public record Opensearch(
        String scheme,
        String host,
        int port,
        String username,
        String password
    ) {
        public Opensearch {
            if (scheme == null) scheme = "https";
            if (port == 0) port = 9200;
        }
    }
}
```

## Conditional bean configuration

Create the `DocumentSearch` bean based on the `documentsearch.type` property:

```java
@Configuration
@EnableConfigurationProperties(DocumentSearchProperties.class)
public class DocumentSearchConfig {

    @Bean
    @ConditionalOnProperty(name = "documentsearch.type", havingValue = "elasticsearch")
    DocumentSearch elasticsearchDocumentSearch(DocumentSearchProperties properties) {
        var es = properties.elasticsearch();
        return DocumentSearch.ofElasticSearch(
            ElasticSearchConfiguration.builder()
                .serverUrl(es.serverUrl())
                .apiKey(es.apiKey())
                .build());
    }

    @Bean
    @ConditionalOnProperty(name = "documentsearch.type", havingValue = "opensearch")
    DocumentSearch opensearchDocumentSearch(DocumentSearchProperties properties) {
        var os = properties.opensearch();
        return DocumentSearch.ofOpenSearch(
            OpenSearchConfiguration.builder()
                .scheme(os.scheme())
                .host(os.host())
                .port(os.port())
                .username(os.username())
                .password(os.password())
                .build());
    }
}
```

## Application properties

```yaml
documentsearch:
  type: opensearch          # or "elasticsearch"
  elasticsearch:
    server-url: https://localhost:9200
    api-key: my-api-key
  opensearch:
    scheme: https
    host: localhost
    port: 9200
    username: admin
    password: admin
```

Now you can inject `DocumentSearch` anywhere and swap backends by changing a single property value.
