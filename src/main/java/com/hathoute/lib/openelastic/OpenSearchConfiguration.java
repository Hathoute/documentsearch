package com.hathoute.lib.openelastic;

/**
 * Configuration for an OpenSearch backend.
 *
 * <p>Example: {@code OpenSearchConfiguration.builder()
 *     .scheme("https").host("localhost").port(9200)
 *     .username("admin").password("admin")
 *     .skipSslVerification(true).build();}</p>
 */
public final class OpenSearchConfiguration {

    private final String scheme;
    private final String host;
    private final int port;
    private final String username;
    private final String password;

    private OpenSearchConfiguration(Builder builder) {
        this.scheme = builder.scheme;
        this.host = builder.host;
        this.port = builder.port;
        this.username = builder.username;
        this.password = builder.password;
    }

    public String scheme() {
        return scheme;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public String username() {
        return username;
    }

    public String password() {
        return password;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String scheme = "https";
        private String host;
        private int port = 9200;
        private String username;
        private String password;

        public Builder scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder port(int port) {
            this.port = port;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public OpenSearchConfiguration build() {
            if (host == null || host.isBlank()) {
                throw new IllegalArgumentException("host must be set");
            }
            if (port <= 0) {
                throw new IllegalArgumentException("port must be positive");
            }
            return new OpenSearchConfiguration(this);
        }
    }
}
