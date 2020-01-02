package de.magicline.racoon.config;

import de.magicline.racoon.domain.provider.dto.ValidationMode;

import java.net.URI;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.provider")
public class ProviderProperties {

    private String notifyURL;
    private String notifyEmail;
    private String apiKey;
    private ValidationMode validationMode;
    private Uris uris = new Uris();
    private Retries retries = new Retries();

    public String getNotifyURL() {
        return notifyURL;
    }

    public void setNotifyURL(String notifyURL) {
        this.notifyURL = notifyURL;
    }

    public String getNotifyEmail() {
        return notifyEmail;
    }

    public void setNotifyEmail(String notifyEmail) {
        this.notifyEmail = notifyEmail;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public ValidationMode getValidationMode() {
        return validationMode;
    }

    public void setValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    public Uris getUris() {
        return uris;
    }

    public void setUris(Uris uris) {
        this.uris = uris;
    }

    public Retries getRetries() {
        return retries;
    }

    public void setRetries(Retries retries) {
        this.retries = retries;
    }

    public static class Uris {

        private URI sync;
        private URI async;
        private URI results;

        public Uris() {
            super();
        }

        public Uris(URI sync, URI async, URI results) {
            this.sync = sync;
            this.async = async;
            this.results = results;
        }

        public URI getSync() {
            return sync;
        }

        public void setSync(URI sync) {
            this.sync = sync;
        }

        public URI getAsync() {
            return async;
        }

        public void setAsync(URI async) {
            this.async = async;
        }

        public URI getResults() {
            return results;
        }

        public void setResults(URI results) {
            this.results = results;
        }
    }

    public static class Retries {

        private int maxAttempts;
        private long initialIntervalSec;

        public Retries() {
            super();
        }

        public Retries(int maxAttempts, int initialIntervalSec) {
            this.maxAttempts = maxAttempts;
            this.initialIntervalSec = initialIntervalSec;
        }

        public int getMaxAttempts() {
            return maxAttempts;
        }

        public void setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
        }

        public long getInitialIntervalSec() {
            return initialIntervalSec;
        }

        public void setInitialIntervalSec(long initialIntervalSec) {
            this.initialIntervalSec = initialIntervalSec;
        }
    }
}
