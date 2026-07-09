package com.hathoute.lib.openelastic;

import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestClientBuilder;
import org.opensearch.client.json.JsonpMapper;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;
import org.opensearch.client.transport.rest_client.RestClientTransport;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.StringReader;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Objects;

/**
 * {@link DocumentSearch} backed by the official {@code opensearch-java}
 * client (built on the Apache HttpClient 5 based low-level REST client).
 */
final class OpenSearchDocumentSearch implements DocumentSearch {

    private final OpenSearchClient client;
    private final RestClient restClient;

    OpenSearchDocumentSearch(OpenSearchConfiguration configuration) {
        HttpHost host = new HttpHost(configuration.scheme(), configuration.host(),
                configuration.port());

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(host),
                new UsernamePasswordCredentials(configuration.username(),
                        passwordChars(configuration.password())));

        RestClientBuilder builder = RestClient.builder(host)
                .setHttpClientConfigCallback(httpClientBuilder -> configure(
                        httpClientBuilder, credentialsProvider, configuration.skipSslVerification()));

        this.restClient = builder.build();
        RestClientTransport transport =
                new RestClientTransport(restClient, new JacksonJsonpMapper());
        this.client = new OpenSearchClient(transport);
    }

    private static HttpAsyncClientBuilder configure(HttpAsyncClientBuilder httpClientBuilder,
                                                    BasicCredentialsProvider credentialsProvider,
                                                    boolean skipSslVerification) {
        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        if (skipSslVerification) {
            try {
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[]{trustAllManager()}, new SecureRandom());
                TlsStrategy tlsStrategy = ClientTlsStrategyBuilder.create()
                        .setSslContext(sslContext)
                        .setHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                        .build();
                httpClientBuilder.setConnectionManager(
                        PoolingAsyncClientConnectionManagerBuilder.create()
                                .setTlsStrategy(tlsStrategy)
                                .build());
            } catch (Exception e) {
                throw new DocumentSearchException("Failed to configure SSL context", e);
            }
        }
        return httpClientBuilder;
    }

    @Override
    public void createIndex(String index) {
        try {
            client.indices().create(c -> c.index(index));
        } catch (Exception e) {
            throw wrap("createIndex", e);
        }
    }

    @Override
    public <T> String index(String index, String id, T document) {
        try {
            var response = client.index(i -> {
                i.index(index);
                if (id != null) {
                    i.id(id);
                }
                return i.document(document);
            });
            return response.id();
        } catch (Exception e) {
            throw wrap("index", e);
        }
    }

    @Override
    public <T> void update(String index, String id, T document) {
        try {
            client.update(u -> u.index(index).id(id).doc(document),
                    nullSafeClass(document));
        } catch (Exception e) {
            throw wrap("update", e);
        }
    }

    @Override
    public void delete(String index, String id) {
        try {
            client.delete(d -> d.index(index).id(id));
        } catch (Exception e) {
            throw wrap("delete", e);
        }
    }

    @Override
    public void deleteIndex(String index) {
        try {
            client.indices().delete(d -> d.index(index));
        } catch (Exception e) {
            throw wrap("deleteIndex", e);
        }
    }

    @Override
    public <T> List<T> search(String index, Class<T> documentClass) {
        return doSearch(index,
                org.opensearch.client.opensearch._types.query_dsl.Query
                        .of(b -> b.matchAll(m -> m)),
                documentClass);
    }

    @Override
    public <T> List<T> search(String index, Query query, Class<T> documentClass) {
        return doSearch(index, toOsQuery(query), documentClass);
    }

    @Override
    public <T> List<T> search(String index, String jsonQuery, Class<T> documentClass) {
        return doSearch(index, parseOsQuery(jsonQuery), documentClass);
    }

    @Override
    public <T> void bulk(String index, List<BulkEntry<T>> entries) {
        try {
            var builder = new org.opensearch.client.opensearch.core.BulkRequest.Builder()
                    .index(index);
            for (BulkEntry<T> entry : entries) {
                builder.operations(op -> op.index(io -> io
                        .index(index)
                        .id(entry.id())
                        .document(entry.document())));
            }
            client.bulk(builder.build());
        } catch (Exception e) {
            throw wrap("bulk", e);
        }
    }

    @Override
    public void close() {
        try {
            restClient.close();
        } catch (IOException e) {
            throw wrap("close", e);
        }
    }

    // -- internals -------------------------------------------------------------

    private <T> List<T> doSearch(String index,
                                 org.opensearch.client.opensearch._types.query_dsl.Query osQuery,
                                 Class<T> documentClass) {
        try {
            SearchResponse<T> response = client.search(
                    s -> s.index(index).query(osQuery), documentClass);
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            throw wrap("search", e);
        }
    }

    private org.opensearch.client.opensearch._types.query_dsl.Query toOsQuery(Query query) {
        if (query instanceof MatchAllQuery) {
            return org.opensearch.client.opensearch._types.query_dsl.Query
                    .of(b -> b.matchAll(m -> m));
        }
        if (query instanceof MatchQuery m) {
            return org.opensearch.client.opensearch._types.query_dsl.Query
                    .of(b -> b.match(mm -> mm.field(m.field()).query(fieldValue(m.value()))));
        }
        if (query instanceof TermQuery t) {
            return org.opensearch.client.opensearch._types.query_dsl.Query
                    .of(b -> b.term(tm -> tm.field(t.field()).value(fieldValue(t.value()))));
        }
        if (query instanceof BoolQuery bq) {
            return org.opensearch.client.opensearch._types.query_dsl.Query
                    .of(b -> b.bool(bb -> {
                        if (!bq.must().isEmpty()) {
                            bb.must(bq.must().stream().map(this::toOsQuery).toList());
                        }
                        if (!bq.should().isEmpty()) {
                            bb.should(bq.should().stream().map(this::toOsQuery).toList());
                        }
                        if (!bq.filter().isEmpty()) {
                            bb.filter(bq.filter().stream().map(this::toOsQuery).toList());
                        }
                        return bb;
                    }));
        }
        throw new IllegalStateException("Unsupported query type: " + query);
    }

    private static FieldValue fieldValue(Object value) {
        if (value == null) {
            return FieldValue.NULL;
        }
        if (value instanceof Boolean b) {
            return FieldValue.of(b);
        }
        if (value instanceof Number n) {
            if (value instanceof Long || value instanceof Integer
                    || value instanceof Short || value instanceof Byte) {
                return FieldValue.of(n.longValue());
            }
            return FieldValue.of(n.doubleValue());
        }
        return FieldValue.of(value.toString());
    }

    private org.opensearch.client.opensearch._types.query_dsl.Query parseOsQuery(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("jsonQuery must not be blank");
        }
        JsonpMapper mapper = client._transport().jsonpMapper();
        try (StringReader reader = new StringReader(json)) {
            var parser = mapper.jsonProvider().createParser(reader);
            return org.opensearch.client.opensearch._types.query_dsl.Query._DESERIALIZER
                    .deserialize(parser, mapper);
        }
    }

    private static char[] passwordChars(String password) {
        return password == null ? new char[0] : password.toCharArray();
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> nullSafeClass(T document) {
        return (Class<T>) document.getClass();
    }

    private static X509TrustManager trustAllManager() {
        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                // no-op
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                // no-op
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }

    private static DocumentSearchException wrap(String operation, Throwable cause) {
        return new DocumentSearchException(
                "OpenSearch '" + operation + "' failed: " + cause.getMessage(), cause);
    }
}
