package com.hathoute.lib.openelastic.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonpMapper;
import com.hathoute.lib.openelastic.BulkEntry;
import com.hathoute.lib.openelastic.DocumentSearch;
import com.hathoute.lib.openelastic.DocumentSearchException;
import com.hathoute.lib.openelastic.query.BoolQuery;
import com.hathoute.lib.openelastic.query.DateRangeQuery;
import com.hathoute.lib.openelastic.query.MatchAllQuery;
import com.hathoute.lib.openelastic.query.MatchQuery;
import com.hathoute.lib.openelastic.query.NumberRangeQuery;
import com.hathoute.lib.openelastic.query.Query;
import com.hathoute.lib.openelastic.query.TermQuery;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;

/**
 * {@link DocumentSearch} backed by the official {@code elasticsearch-java}
 * client.
 */
public final class ElasticSearchDocumentSearch implements DocumentSearch {

    private final ElasticsearchClient client;

    public ElasticSearchDocumentSearch(ElasticSearchConfiguration configuration) {
        this.client = ElasticsearchClient.of(b -> b
                .host(configuration.serverUrl())
                .apiKey(configuration.apiKey()));
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
                    (Class<T>) nullSafeClass(document));
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
                co.elastic.clients.elasticsearch._types.query_dsl.Query
                        .of(b -> b.matchAll(m -> m)),
                documentClass);
    }

    @Override
    public <T> List<T> search(String index, Query query, Class<T> documentClass) {
        return doSearch(index, toEsQuery(query), documentClass);
    }

    @Override
    public <T> List<T> search(String index, String jsonQuery, Class<T> documentClass) {
        return doSearch(index, parseEsQuery(jsonQuery), documentClass);
    }

    @Override
    public <T> void bulk(String index, List<BulkEntry<T>> entries) {
        try {
            var builder = new co.elastic.clients.elasticsearch.core.BulkRequest.Builder()
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
            client.close();
        } catch (IOException e) {
            throw wrap("close", e);
        }
    }

    // -- internals -------------------------------------------------------------

    private <T> List<T> doSearch(String index,
                                 co.elastic.clients.elasticsearch._types.query_dsl.Query esQuery,
                                 Class<T> documentClass) {
        try {
            SearchResponse<T> response = client.search(
                    s -> s.index(index).query(esQuery), documentClass);
            return response.hits().hits().stream()
                    .map(Hit::source)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception e) {
            throw wrap("search", e);
        }
    }

    private co.elastic.clients.elasticsearch._types.query_dsl.Query toEsQuery(Query query) {
        if (query instanceof MatchAllQuery) {
            return co.elastic.clients.elasticsearch._types.query_dsl.Query
                    .of(b -> b.matchAll(m -> m));
        }
        if (query instanceof MatchQuery m) {
            return co.elastic.clients.elasticsearch._types.query_dsl.Query
                    .of(b -> b.match(mm -> mm.field(m.field()).query(fieldValue(m.value()))));
        }
        if (query instanceof TermQuery t) {
            return co.elastic.clients.elasticsearch._types.query_dsl.Query
                    .of(b -> b.term(tm -> tm.field(t.field()).value(fieldValue(t.value()))));
        }
        if (query instanceof BoolQuery bq) {
            return co.elastic.clients.elasticsearch._types.query_dsl.Query
                    .of(b -> b.bool(bb -> {
                        if (!bq.must().isEmpty()) {
                            bb.must(bq.must().stream().map(this::toEsQuery).toList());
                        }
                        if (!bq.should().isEmpty()) {
                            bb.should(bq.should().stream().map(this::toEsQuery).toList());
                        }
                        if (!bq.filter().isEmpty()) {
                            bb.filter(bq.filter().stream().map(this::toEsQuery).toList());
                        }
                        return bb;
                    }));
        }
        if (query instanceof NumberRangeQuery r) {
            return co.elastic.clients.elasticsearch._types.query_dsl.Query
                    .of(b -> b.range(rb -> rb.number(nrb -> {
                        nrb.field(r.field());
                        if (r.gt() != null) nrb.gt(r.gt());
                        if (r.gte() != null) nrb.gte(r.gte());
                        if (r.lt() != null) nrb.lt(r.lt());
                        if (r.lte() != null) nrb.lte(r.lte());
                        return nrb;
                    })));
        }
        if (query instanceof DateRangeQuery r) {
            return co.elastic.clients.elasticsearch._types.query_dsl.Query
                    .of(b -> b.range(rb -> rb.date(drb -> {
                        drb.field(r.field());
                        if (r.gt() != null) drb.gt(r.gt());
                        if (r.gte() != null) drb.gte(r.gte());
                        if (r.lt() != null) drb.lt(r.lt());
                        if (r.lte() != null) drb.lte(r.lte());
                        if (r.format() != null) drb.format(r.format());
                        if (r.timeZone() != null) drb.timeZone(r.timeZone());
                        return drb;
                    })));
        }
        throw new IllegalStateException("Unsupported query type: " + query);
    }

    private static FieldValue fieldValue(Object value) {
        if (value == null) {
            return FieldValue.NULL;
        }
        return FieldValue.of(value);
    }

    private co.elastic.clients.elasticsearch._types.query_dsl.Query parseEsQuery(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("jsonQuery must not be blank");
        }
        JsonpMapper mapper = client._transport().jsonpMapper();
        try (StringReader reader = new StringReader(json)) {
            var parser = mapper.jsonProvider().createParser(reader);
            return co.elastic.clients.elasticsearch._types.query_dsl.Query._DESERIALIZER
                    .deserialize(parser, mapper);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> nullSafeClass(T document) {
        return (Class<T>) document.getClass();
    }

    private static DocumentSearchException wrap(String operation, Throwable cause) {
        return new DocumentSearchException(
                "ElasticSearch '" + operation + "' failed: " + cause.getMessage(), cause);
    }
}
