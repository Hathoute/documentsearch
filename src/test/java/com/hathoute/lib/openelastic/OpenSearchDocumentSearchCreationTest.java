package com.hathoute.lib.openelastic;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OpenSearchDocumentSearchCreationTest {

    @Test
    void buildsWhenSchemeIsNull() {
        var config = OpenSearchConfiguration.builder()
                .scheme(null)
                .host("localhost")
                .build();
        try (var search = DocumentSearch.ofOpenSearch(config)) {
            assertThat(search).isNotNull();
        }
    }

    @Test
    void buildsWithHostOnly() {
        var config = OpenSearchConfiguration.builder().host("localhost").build();
        assertThat(config.scheme()).isEqualTo("https");
        assertThat(config.port()).isEqualTo(9200);
        try (var search = DocumentSearch.ofOpenSearch(config)) {
            assertThat(search).isNotNull();
        }
    }

    @Test
    void buildsWithUsernameAndPassword() {
        var config = OpenSearchConfiguration.builder()
                .host("localhost")
                .username("admin")
                .password("admin")
                .build();
        try (var search = DocumentSearch.ofOpenSearch(config)) {
            assertThat(search).isNotNull();
        }
    }

    @Test
    void buildsWithCustomSchemeAndPort() {
        var config = OpenSearchConfiguration.builder()
                .scheme("http")
                .host("localhost")
                .port(9201)
                .build();
        try (var search = DocumentSearch.ofOpenSearch(config)) {
            assertThat(search).isNotNull();
        }
    }

    @Test
    void failsWhenHostIsNull() {
        assertThatThrownBy(() -> OpenSearchConfiguration.builder().build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void failsWhenHostIsBlank() {
        assertThatThrownBy(() -> OpenSearchConfiguration.builder().host("  ").build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void failsWhenPortIsZero() {
        assertThatThrownBy(() ->
                OpenSearchConfiguration.builder().host("localhost").port(0).build())
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void failsWhenPortIsNegative() {
        assertThatThrownBy(() ->
                OpenSearchConfiguration.builder().host("localhost").port(-1).build())
                .isInstanceOf(IllegalArgumentException.class);
    }
}
