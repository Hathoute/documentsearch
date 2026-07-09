package com.hathoute.lib.openelastic;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.GetResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for {@link ElasticSearchDocumentSearch} backed by a real
 * ElasticSearch node started via Testcontainers.
 *
 * <p>Documents are written through the {@link DocumentSearch} abstraction and
 * fetched back through the native {@code ElasticsearchClient} (the Elastic
 * "high level" client) to independently verify the result.</p>
 */
@Testcontainers
class ElasticSearchDocumentSearchIT {

    private static final String IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:9.3.0";

    @Container
    static final ElasticsearchContainer container =
            new ElasticsearchContainer(IMAGE)
                    // Run the node without security so the abstraction's apiKey
                    // header is simply ignored and we connect over plain HTTP.
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("xpack.security.http.ssl.enabled", "false")
                    .withEnv("xpack.security.transport.ssl.enabled", "false")
                    .withEnv("discovery.type", "single-node")
                    .withStartupTimeout(Duration.ofMinutes(3));

    private static String url;
    private static DocumentSearch search;
    // The native Elastic high level client, used to fetch and verify data
    // independently from the DocumentSearch abstraction under test.
    private static ElasticsearchClient raw;

    @BeforeAll
    static void setUp() {
        url = "http://" + container.getHttpHostAddress();
        search = DocumentSearch.ofElasticSearch(
                ElasticSearchConfiguration.builder()
                        .serverUrl(url)
                        .apiKey("test-key")
                        .build());
        raw = ElasticsearchClient.of(b -> b.host(url));
    }

    /** Fetch a document through the native client (independent verification). */
    private Product fetch(String index, String id) throws Exception {
        GetResponse<Product> response = raw.get(g -> g.index(index).id(id), Product.class);
        return response.found() ? response.source() : null;
    }

    /** Force freshly indexed documents to be searchable (search is near-real-time). */
    private void refresh(String index) throws Exception {
        raw.indices().refresh(r -> r.index(index));
    }

    @Test
    void createAndDeleteIndex() throws Exception {
        String index = "it-create-index";
        search.createIndex(index);
        assertTrue(raw.indices().exists(e -> e.index(index)).value(),
                "index should exist after createIndex");
        search.deleteIndex(index);
        assertFalse(raw.indices().exists(e -> e.index(index)).value(),
                "index should not exist after deleteIndex");
    }

    @Test
    void indexThenFetchViaHighLevelClient() throws Exception {
        String index = "it-index";
        search.createIndex(index);

        String id = search.index(index, "bk-1", new Product("bk-1", "City bike", 123.0));
        assertEquals("bk-1", id);

        Product found = fetch(index, "bk-1");
        assertNotNull(found, "document should be found via the high level client");
        assertEquals("City bike", found.getName());
        assertEquals(123.0, found.getPrice());

        search.deleteIndex(index);
    }

    @Test
    void updateChangesFields() throws Exception {
        String index = "it-update";
        search.createIndex(index);
        search.index(index, "1", new Product("1", "City bike", 100.0));

        search.update(index, "1", new Product("1", "Mountain bike", 150.0));

        Product found = fetch(index, "1");
        assertNotNull(found);
        assertEquals("Mountain bike", found.getName());
        assertEquals(150.0, found.getPrice());

        search.deleteIndex(index);
    }

    @Test
    void deleteRemovesDocument() throws Exception {
        String index = "it-delete";
        search.createIndex(index);
        search.index(index, "1", new Product("1", "Bike", 10.0));

        search.delete(index, "1");

        assertNull(fetch(index, "1"), "document should be gone after delete");

        search.deleteIndex(index);
    }

    @Test
    void bulkIndexesAllDocuments() throws Exception {
        String index = "it-bulk";
        search.createIndex(index);

        search.bulk(index, List.of(
                BulkEntry.of("a", new Product("a", "Alpha", 1.0)),
                BulkEntry.of("b", new Product("b", "Beta", 2.0)),
                BulkEntry.of("c", new Product("c", "Gamma", 3.0))));

        assertEquals(new Product("a", "Alpha", 1.0), fetch(index, "a"));
        assertEquals(new Product("b", "Beta", 2.0), fetch(index, "b"));
        assertEquals(new Product("c", "Gamma", 3.0), fetch(index, "c"));

        search.deleteIndex(index);
    }

    @Test
    void searchAllReturnsEveryDocument() throws Exception {
        String index = "it-search-all";
        search.createIndex(index);
        search.bulk(index, List.of(
                BulkEntry.of("1", new Product("1", "City bike", 10.0)),
                BulkEntry.of("2", new Product("2", "City car", 20.0))));
        refresh(index);

        List<Product> results = search.search(index, Product.class);
        assertEquals(2, results.size());

        search.deleteIndex(index);
    }

    @Test
    void searchWithQueryDslMatchesOnlyRelevant() throws Exception {
        String index = "it-search-query";
        search.createIndex(index);
        search.bulk(index, List.of(
                BulkEntry.of("1", new Product("1", "City bike", 10.0)),
                BulkEntry.of("2", new Product("2", "City car", 20.0))));
        refresh(index);

        List<Product> results = search.search(index, Query.match("name", "bike"), Product.class);
        assertEquals(1, results.size());
        assertEquals("1", results.get(0).getSku());

        search.deleteIndex(index);
    }

    @Test
    void searchWithTermQuery() throws Exception {
        String index = "it-search-term";
        search.createIndex(index);
        search.bulk(index, List.of(
                BulkEntry.of("1", new Product("1", "Bike", 10.0)),
                BulkEntry.of("2", new Product("2", "Bike", 20.0)),
                BulkEntry.of("3", new Product("3", "Car", 30.0))));
        refresh(index);

        List<Product> results = search.search(index, Query.term("name", "bike"), Product.class);
        assertEquals(2, results.size());

        search.deleteIndex(index);
    }

    @Test
    void searchWithBoolQuery() throws Exception {
        String index = "it-search-bool";
        search.createIndex(index);
        search.bulk(index, List.of(
                BulkEntry.of("1", new Product("1", "Bike", 10.0)),
                BulkEntry.of("2", new Product("2", "Bike", 20.0)),
                BulkEntry.of("3", new Product("3", "Car", 20.0))));
        refresh(index);

        List<Product> results = search.search(index,
                Query.bool().must(Query.match("name", "bike")).filter(Query.term("price", 20.0)).build(),
                Product.class);
        assertEquals(1, results.size());
        assertEquals("2", results.get(0).getSku());

        search.deleteIndex(index);
    }

    @Test
    void searchWithJsonFallback() throws Exception {
        String index = "it-search-json";
        search.createIndex(index);
        search.bulk(index, List.of(
                BulkEntry.of("1", new Product("1", "City bike", 10.0)),
                BulkEntry.of("2", new Product("2", "Mountain bike", 20.0))));
        refresh(index);

        List<Product> results = search.search(index,
                "{\"match\":{\"name\":\"bike\"}}", Product.class);
        assertEquals(2, results.size());

        search.deleteIndex(index);
    }

    /** Simple document POJO used by the tests. */
    public static class Product {
        private String sku;
        private String name;
        private double price;

        public Product() {
        }

        public Product(String sku, String name, double price) {
            this.sku = sku;
            this.name = name;
            this.price = price;
        }

        public String getSku() {
            return sku;
        }

        public void setSku(String sku) {
            this.sku = sku;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Product other)) {
                return false;
            }
            return Objects.equals(sku, other.sku)
                    && Objects.equals(name, other.name)
                    && price == other.price;
        }

        @Override
        public int hashCode() {
            return Objects.hash(sku, name, price);
        }
    }
}
