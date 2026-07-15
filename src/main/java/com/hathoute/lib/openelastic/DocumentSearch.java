package com.hathoute.lib.openelastic;

import com.hathoute.lib.openelastic.elasticsearch.ElasticSearchConfiguration;
import com.hathoute.lib.openelastic.elasticsearch.ElasticSearchDocumentSearch;
import com.hathoute.lib.openelastic.opensearch.OpenSearchConfiguration;
import com.hathoute.lib.openelastic.opensearch.OpenSearchDocumentSearch;
import com.hathoute.lib.openelastic.query.Query;

import java.util.List;

/**
 * Backend-agnostic abstraction over an ElasticSearch or OpenSearch cluster.
 *
 * <p>Obtain an instance through one of the static factories:</p>
 * <pre>{@code
 * DocumentSearch es = DocumentSearch.ofElasticSearch(
 *     ElasticSearchConfiguration.builder().serverUrl("https://...").apiKey("...").build());
 *
 * DocumentSearch os = DocumentSearch.ofOpenSearch(
 *     OpenSearchConfiguration.builder().host("localhost").port(9200)
 *         .username("admin").password("admin").build());
 * }</pre>
 *
 * <p>Implementations are {@link AutoCloseable}; closing releases the underlying
 * network resources.</p>
 */
public interface DocumentSearch extends AutoCloseable {

    /**
     * Create a client connected to an ElasticSearch backend.
     *
     * @param configuration connection configuration
     * @return a new {@link DocumentSearch}; must be closed by the caller
     */
    static DocumentSearch ofElasticSearch(ElasticSearchConfiguration configuration) {
        return new ElasticSearchDocumentSearch(configuration);
    }

    /**
     * Create a client connected to an OpenSearch backend.
     *
     * @param configuration connection configuration
     * @return a new {@link DocumentSearch}; must be closed by the caller
     */
    static DocumentSearch ofOpenSearch(OpenSearchConfiguration configuration) {
        return new OpenSearchDocumentSearch(configuration);
    }

    /**
     * Create a new index.
     *
     * @param index index name
     */
    void createIndex(String index);

    /**
     * Index a document. If {@code id} is {@code null} the backend generates one.
     *
     * @param index    index name
     * @param id       document id, or {@code null} to let the backend generate one
     * @param document document to index
     * @param <T>      document type
     * @return the document id (generated if {@code id} was {@code null})
     */
    <T> String index(String index, String id, T document);

    /**
     * Partially update a document. Only the fields set on {@code document} are
     * sent to the backend.
     *
     * @param index    index name
     * @param id       document id
     * @param document partial document with the fields to update
     * @param <T>      document type
     */
    <T> void update(String index, String id, T document);

    /**
     * Delete a document by id.
     *
     * @param index index name
     * @param id    document id
     */
    void delete(String index, String id);

    /**
     * Delete an index.
     *
     * @param index index name
     */
    void deleteIndex(String index);

    /**
     * Search an index, returning all matching documents.
     *
     * @param index         index name
     * @param documentClass target document type
     * @param <T>           document type
     * @return matched documents (never {@code null})
     */
    <T> List<T> search(String index, Class<T> documentClass);

    /**
     * Search an index using a {@link Query} from the DSL.
     *
     * @param index         index name
     * @param query         query DSL object
     * @param documentClass target document type
     * @param <T>           document type
     * @return matched documents (never {@code null})
     */
    <T> List<T> search(String index, Query query, Class<T> documentClass);

    /**
     * Search an index using a raw JSON query body (escape hatch for queries not
     * expressible through the DSL).
     *
     * <p>Example: {@code search("products",
     *     "{\"query\":{\"match\":{\"name\":\"bike\"}}}", Product.class);}</p>
     *
     * @param index         index name
     * @param jsonQuery     raw query DSL JSON (the {@code query} object body)
     * @param documentClass target document type
     * @param <T>           document type
     * @return matched documents (never {@code null})
     */
    <T> List<T> search(String index, String jsonQuery, Class<T> documentClass);

    /**
     * Bulk index documents.
     *
     * @param index    index name
     * @param entries  documents to index, each with its id
     * @param <T>      document type
     */
    <T> void bulk(String index, List<BulkEntry<T>> entries);

    /**
     * Release network resources held by this client.
     */
    @Override
    void close();
}
