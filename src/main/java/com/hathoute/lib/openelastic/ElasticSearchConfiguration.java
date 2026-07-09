package com.hathoute.lib.openelastic;

/**
 * Configuration for an ElasticSearch backend.
 *
 * <p>Example: {@code ElasticSearchConfiguration.builder()
 *     .serverUrl("https://localhost:9200").apiKey("...").build();}</p>
 */
public final class ElasticSearchConfiguration {

    private final String serverUrl;
    private final String apiKey;

    private ElasticSearchConfiguration(Builder builder) {
        this.serverUrl = builder.serverUrl;
        this.apiKey = builder.apiKey;
    }

    public String serverUrl() {
        return serverUrl;
    }

    public String apiKey() {
        return apiKey;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String serverUrl;
        private String apiKey;

        public Builder serverUrl(String serverUrl) {
            this.serverUrl = serverUrl;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public ElasticSearchConfiguration build() {
            if (serverUrl == null || serverUrl.isBlank()) {
                throw new IllegalArgumentException("serverUrl must be set");
            }
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalArgumentException("apiKey must be set");
            }
            return new ElasticSearchConfiguration(this);
        }
    }
}
