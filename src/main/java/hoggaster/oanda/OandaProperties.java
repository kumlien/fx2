package hoggaster.oanda;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URL;

@ConfigurationProperties(prefix = "oanda", exceptionIfInvalid = true, locations = "file:/data/fx2/config/application.yml")
public class OandaProperties {

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for reg ex for fetching prices from oanda")
    private String fetchPricesRegex;

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for reg ex for fetching candles from oanda")
    private String fetchCandlesRegex;

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for streaming api url from oanda")
    private String streamingApiUrl;

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for API-Key for oanda")
    private String apiKey;

    @NotEmpty(message = "Something is wrong with the configuration properties, can't read property for main accountID for oanda")
    private String mainAccountId;

    public String getFetchPricesRegex() {
        return fetchPricesRegex;
    }

    public String getStreamingApiUrl() {
        return streamingApiUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getMainAccountId() {
        return mainAccountId;
    }

    public void setFetchPricesRegex(String fetchPricesRegex) {
        this.fetchPricesRegex = fetchPricesRegex;
    }


    public void setStreamingApiUrl(String streamingApiUrl) {
        this.streamingApiUrl = streamingApiUrl;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setMainAccountId(String mainAccountId) {
        this.mainAccountId = mainAccountId;
    }

    public String getFetchCandlesRegex() {
        return fetchCandlesRegex;
    }

    public void setFetchCandlesRegex(String fetchCandlesRegex) {
        this.fetchCandlesRegex = fetchCandlesRegex;
    }

}
