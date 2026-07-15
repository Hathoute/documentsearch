# Version Overrides

open-elastic bundles both the Elasticsearch and OpenSearch Java clients as compile-scoped dependencies. If your project already depends on a different version of either client — or if you need to pin a specific version — you can exclude the transitive dependency and declare your own version.

## Elasticsearch

To override the bundled Elasticsearch Java client (default `9.3.0`):

```xml
<dependency>
    <groupId>com.hathoute.lib</groupId>
    <artifactId>documentsearch</artifactId>
    <version>1.0-SNAPSHOT</version>
    <exclusions>
        <exclusion>
            <groupId>co.elastic.clients</groupId>
            <artifactId>elasticsearch-java</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<dependency>
    <groupId>co.elastic.clients</groupId>
    <artifactId>elasticsearch-java</artifactId>
    <version>9.4.0</version>  <!-- your preferred version -->
</dependency>
```

## OpenSearch

The OpenSearch backend ships as two artifacts: the REST client transport and the Java client. To override both (default `3.7.0` / `3.9.0`):

```xml
<dependency>
    <groupId>com.hathoute.lib</groupId>
    <artifactId>documentsearch</artifactId>
    <version>1.0-SNAPSHOT</version>
    <exclusions>
        <exclusion>
            <groupId>org.opensearch.client</groupId>
            <artifactId>opensearch-rest-client</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.opensearch.client</groupId>
            <artifactId>opensearch-java</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<dependency>
    <groupId>org.opensearch.client</groupId>
    <artifactId>opensearch-rest-client</artifactId>
    <version>3.8.0</version>  <!-- your preferred version -->
</dependency>
<dependency>
    <groupId>org.opensearch.client</groupId>
    <artifactId>opensearch-java</artifactId>
    <version>3.10.0</version>  <!-- your preferred version -->
</dependency>
```

## Using only one backend (reduce footprint)

If you know you will only ever use one backend, you can exclude the other entirely to reduce your dependency footprint:

```xml
<dependency>
    <groupId>com.hathoute.lib</groupId>
    <artifactId>documentsearch</artifactId>
    <version>1.0-SNAPSHOT</version>
    <exclusions>
        <!-- Exclude OpenSearch if you only use Elasticsearch -->
        <exclusion>
            <groupId>org.opensearch.client</groupId>
            <artifactId>opensearch-rest-client</artifactId>
        </exclusion>
        <exclusion>
            <groupId>org.opensearch.client</groupId>
            <artifactId>opensearch-java</artifactId>
        </exclusion>
        <!-- or vice versa -->
        <exclusion>
            <groupId>co.elastic.clients</groupId>
            <artifactId>elasticsearch-java</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

## Verifying the effective version

Run the Maven dependency tree to confirm the correct version is resolved:

```bash
mvn dependency:tree -Dincludes=co.elastic.clients:elasticsearch-java
mvn dependency:tree -Dincludes=org.opensearch.client:*
```
