package de.magicline.racoon.common;

import de.magicline.racoon.config.ProviderProperties;
import de.magicline.racoon.domain.provider.dto.ValidationMode;

@SuppressWarnings("unused")
public final class ProviderPropertiesBuilder {

    private String notifyURL;
    private String notifyEmail;
    private String apiKey = "ev-7791b803c271ab303acfa5029b1847e1";
    private ValidationMode validationMode = ValidationMode.EXPRESS;
    private ProviderProperties.Uris uris = new ProviderProperties.Uris();
    private ProviderProperties.Retries retries = new ProviderProperties.Retries(2, 1);

    public static ProviderPropertiesBuilder builder() {
        return new ProviderPropertiesBuilder();
    }

    public ProviderPropertiesBuilder withNotifyURL(String notifyURL) {
        this.notifyURL = notifyURL;
        return this;
    }

    public ProviderPropertiesBuilder withNotifyEmail(String notifyEmail) {
        this.notifyEmail = notifyEmail;
        return this;
    }

    public ProviderPropertiesBuilder withApiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public ProviderPropertiesBuilder withValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
        return this;
    }

    public ProviderPropertiesBuilder withUri(ProviderProperties.Uris uris) {
        this.uris = uris;
        return this;
    }

    public ProviderPropertiesBuilder withRetry(ProviderProperties.Retries retries) {
        this.retries = retries;
        return this;
    }

    public ProviderProperties build() {
        ProviderProperties providerProperties = new ProviderProperties();
        providerProperties.setNotifyURL(notifyURL);
        providerProperties.setNotifyEmail(notifyEmail);
        providerProperties.setApiKey(apiKey);
        providerProperties.setValidationMode(validationMode);
        providerProperties.setUris(uris);
        providerProperties.setRetries(retries);
        return providerProperties;
    }
}
