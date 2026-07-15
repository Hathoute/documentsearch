package com.hathoute.lib.openelastic.elasticsearch;

import com.hathoute.lib.openelastic.DocumentSearch;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElasticSearchDocumentSearchCreationTest {

    @Test
    void buildsWithServerUrlAndApiKey() {
        var config = ElasticSearchConfiguration.builder()
                .serverUrl("http://localhost:9200")
                .apiKey("test-key")
                .build();
        try (var search = DocumentSearch.ofElasticSearch(config)) {
            assertThat(search).isNotNull();
        }
    }

    @Test
    void failsWhenServerUrlIsNull() {
        assertThatThrownBy(() ->
                ElasticSearchConfiguration.builder().apiKey("key").build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void failsWhenServerUrlIsBlank() {
        assertThatThrownBy(() ->
                ElasticSearchConfiguration.builder().serverUrl("  ").apiKey("key").build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void failsWhenApiKeyIsNull() {
        assertThatThrownBy(() ->
                ElasticSearchConfiguration.builder().serverUrl("http://localhost:9200").build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void failsWhenApiKeyIsBlank() {
        assertThatThrownBy(() ->
                ElasticSearchConfiguration.builder()
                        .serverUrl("http://localhost:9200")
                        .apiKey("  ")
                        .build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
