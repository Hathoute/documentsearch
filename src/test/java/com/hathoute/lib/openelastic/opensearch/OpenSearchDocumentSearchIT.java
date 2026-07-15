package com.hathoute.lib.openelastic.opensearch;

import com.hathoute.lib.openelastic.BulkEntry;
import com.hathoute.lib.openelastic.DocumentSearch;
import com.hathoute.lib.openelastic.query.Query;
import org.apache.hc.core5.http.HttpHost;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opensearch.client.RestClient;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch.core.GetResponse;
import org.opensearch.client.transport.rest_client.RestClientTransport;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.in;

@Testcontainers
class OpenSearchDocumentSearchIT {

    private static final String IMAGE = "opensearchproject/opensearch:2.19.0";

    @Container
    @SuppressWarnings("resource")
    static final GenericContainer<?> container =
            new GenericContainer<>(IMAGE)
                    .withEnv("discovery.type", "single-node")
                    .withEnv("DISABLE_SECURITY_PLUGIN", "true")
                    .withExposedPorts(9200)
                    .withStartupTimeout(Duration.ofMinutes(3));

    private static DocumentSearch search;
    private static OpenSearchClient raw;

    @BeforeAll
    static void setUp() {
        String host = container.getHost();
        int port = container.getMappedPort(9200);
        search = DocumentSearch.ofOpenSearch(
                OpenSearchConfiguration.builder()
                        .scheme("http")
                        .host(host)
                        .port(port)
                        .build());
        raw = new OpenSearchClient(
                new RestClientTransport(
                        RestClient.builder(new HttpHost("http", host, port)).build(),
                        new JacksonJsonpMapper()));
    }

    private Product fetch(String index, String id) throws Exception {
        GetResponse<Product> response = raw.get(g -> g.index(index).id(id), Product.class);
        return response.found() ? response.source() : null;
    }

    private void refresh(String index) throws Exception {
        raw.indices().refresh(r -> r.index(index));
    }

    @Test
    void createAndDeleteIndex() throws Exception {
        String index = "it-create-index";
        search.createIndex(index);
        assertThat(raw.indices().exists(e -> e.index(index)).value())
                .as("index should exist after createIndex")
                .isTrue();
        search.deleteIndex(index);
        assertThat(raw.indices().exists(e -> e.index(index)).value())
                .as("index should not exist after deleteIndex")
                .isFalse();
    }

    @Test
    void indexThenFetchViaHighLevelClient() throws Exception {
        String index = "it-index";
        search.createIndex(index);

        String id = search.index(index, "bk-1", new Product("bk-1", "City bike", 123.0));
        assertThat(id).isEqualTo("bk-1");

        Product found = fetch(index, "bk-1");
        assertThat(found).as("document should be found via the high level client").isNotNull();
        assertThat(found.getName()).isEqualTo("City bike");
        assertThat(found.getPrice()).isEqualTo(123.0);

        search.deleteIndex(index);
    }

    @Test
    void updateChangesFields() throws Exception {
        String index = "it-update";
        search.createIndex(index);
        search.index(index, "1", new Product("1", "City bike", 100.0));

        search.update(index, "1", new Product("1", "Mountain bike", 150.0));

        Product found = fetch(index, "1");
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Mountain bike");
        assertThat(found.getPrice()).isEqualTo(150.0);

        search.deleteIndex(index);
    }

    @Test
    void deleteRemovesDocument() throws Exception {
        String index = "it-delete";
        search.createIndex(index);
        search.index(index, "1", new Product("1", "Bike", 10.0));

        search.delete(index, "1");

        assertThat(fetch(index, "1")).as("document should be gone after delete").isNull();

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

        assertThat(fetch(index, "a")).isEqualTo(new Product("a", "Alpha", 1.0));
        assertThat(fetch(index, "b")).isEqualTo(new Product("b", "Beta", 2.0));
        assertThat(fetch(index, "c")).isEqualTo(new Product("c", "Gamma", 3.0));

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
        assertThat(results).hasSize(2);

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
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSku()).isEqualTo("1");

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
        assertThat(results).hasSize(2);

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
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getSku()).isEqualTo("2");

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
        assertThat(results).hasSize(2);

        search.deleteIndex(index);
    }

    @Test
    void searchWithGtRange() throws Exception {
        String index = "it-range-gt";
        search.createIndex(index);
        search.bulk(index, List.of(
                BulkEntry.of("1", new Product("1", "A", 10.0)),
                BulkEntry.of("2", new Product("2", "B", 20.0)),
                BulkEntry.of("3", new Product("3", "C", 30.0))));
        refresh(index);

        List<Product> results = search.search(index, Query.gt("price", 15.0), Product.class);
        assertThat(results).hasSize(2);
        assertThat(results.stream().map(Product::getSku)).containsExactlyInAnyOrder("2", "3");

        search.deleteIndex(index);
    }

    @Test
    void searchWithGteRange() throws Exception {
        String index = "it-range-gte";
        search.createIndex(index);
        search.bulk(index, List.of(
                BulkEntry.of("1", new Product("1", "A", 10.0)),
                BulkEntry.of("2", new Product("2", "B", 20.0)),
                BulkEntry.of("3", new Product("3", "C", 20.0))));
        refresh(index);

        List<Product> results = search.search(index, Query.gte("price", 20.0), Product.class);
        assertThat(results).hasSize(2);

        search.deleteIndex(index);
    }

    @Test
    void searchWithLtRange() throws Exception {
        String index = "it-range-lt";
        search.createIndex(index);
        search.bulk(index, List.of(
                BulkEntry.of("1", new Product("1", "A", 10.0)),
                BulkEntry.of("2", new Product("2", "B", 20.0)),
                BulkEntry.of("3", new Product("3", "C", 30.0))));
        refresh(index);

        List<Product> results = search.search(index, Query.lt("price", 25.0), Product.class);
        assertThat(results).hasSize(2);
        assertThat(results.stream().map(Product::getSku)).containsExactlyInAnyOrder("1", "2");

        search.deleteIndex(index);
    }

    @Test
    void searchWithLteRange() throws Exception {
        String index = "it-range-lte";
        search.createIndex(index);
        search.bulk(index, List.of(
                BulkEntry.of("1", new Product("1", "A", 10.0)),
                BulkEntry.of("2", new Product("2", "B", 20.0)),
                BulkEntry.of("3", new Product("3", "C", 30.0))));
        refresh(index);

        List<Product> results = search.search(index, Query.lte("price", 20.0), Product.class);
        assertThat(results).hasSize(2);
        assertThat(results.stream().map(Product::getSku)).containsExactlyInAnyOrder("1", "2");

        search.deleteIndex(index);
    }

    @Test
    void searchWithCombinedRange() throws Exception {
        String index = "it-range-combined";
        search.createIndex(index);
        search.bulk(index, List.of(
                BulkEntry.of("1", new Product("1", "A", 5.0)),
                BulkEntry.of("2", new Product("2", "B", 10.0)),
                BulkEntry.of("3", new Product("3", "C", 15.0)),
                BulkEntry.of("4", new Product("4", "D", 20.0))));
        refresh(index);

        List<Product> results = search.search(index,
                Query.num("price").gte(10.0).lt(20.0).build(), Product.class);
        assertThat(results).hasSize(2);
        assertThat(results.stream().map(Product::getSku)).containsExactlyInAnyOrder("2", "3");

        search.deleteIndex(index);
    }

    @Test
    void searchWithDateRange() throws Exception {
        String index = "it-range-date";
        search.createIndex(index);
        search.bulk(index, List.of(
                BulkEntry.of("1", new Product("1", "A", 10.0, "2024-01-15")),
                BulkEntry.of("2", new Product("2", "B", 20.0, "2024-06-15")),
                BulkEntry.of("3", new Product("3", "C", 30.0, "2024-12-15"))));
        refresh(index);

        List<Product> results = search.search(index,
                Query.date("creationDate").gte("2024-06-01").lt("2025-01-01")
                        .format("yyyy-MM-dd").timeZone("UTC").build(),
                Product.class);
        assertThat(results).hasSize(2);
        assertThat(results.stream().map(Product::getSku)).containsExactlyInAnyOrder("2", "3");

        search.deleteIndex(index);
    }

    @Test
    void searchWithCustomDateFormat() throws Exception {
        String index = "it-range-date-format";
        search.createIndex(index);
        search.bulk(index, List.of(
                BulkEntry.of("1", new Product("1", "A", 10.0, "2023-01-15")),
                BulkEntry.of("2", new Product("2", "B", 20.0, "2024-10-15")),
                BulkEntry.of("3", new Product("3", "C", 30.0, "2024-12-15"))));
        refresh(index);

        List<Product> results = search.search(index,
                Query.date("creationDate").gte("01-10-2024")
                        .format("MM-dd-yyyy").timeZone("UTC").build(),
                Product.class);
        assertThat(results).hasSize(2);
        assertThat(results.stream().map(Product::getSku)).containsExactlyInAnyOrder("2", "3");

        search.deleteIndex(index);
    }

    /** Simple document POJO used by the tests. */
    public static class Product {
        private String sku;
        private String name;
        private double price;
        private String creationDate;

        public Product() {
        }

        public Product(String sku, String name, double price) {
            this.sku = sku;
            this.name = name;
            this.price = price;
        }

        public Product(String sku, String name, double price, String creationDate) {
            this.sku = sku;
            this.name = name;
            this.price = price;
            this.creationDate = creationDate;
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

        public String getCreationDate() {
            return creationDate;
        }

        public void setCreationDate(String creationDate) {
            this.creationDate = creationDate;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Product other)) {
                return false;
            }
            return Objects.equals(sku, other.sku)
                    && Objects.equals(name, other.name)
                    && price == other.price
                    && Objects.equals(creationDate, other.creationDate);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sku, name, price, creationDate);
        }
    }
}
