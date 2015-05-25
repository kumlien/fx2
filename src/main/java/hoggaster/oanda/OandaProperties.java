package hoggaster.oanda;

import java.net.URL;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oanda", exceptionIfInvalid=true)
public class OandaProperties {

	private String fetchPricesRegex;
	
	private String fetchCandlesRegex;

	private URL restApiUrl;

	private String streamingApiUrl;

	private String apiKey;
	
	private Integer mainAccountId;

	public String getFetchPricesRegex() {
		return fetchPricesRegex;
	}


	public URL getRestApiUrl() {
		return restApiUrl;
	}

	public String getStreamingApiUrl() {
		return streamingApiUrl;
	}

	

	public String getApiKey() {
		return apiKey;
	}


	public Integer getMainAccountId() {
		return mainAccountId;
	}


	public void setFetchPricesRegex(String fetchPricesRegex) {
		this.fetchPricesRegex = fetchPricesRegex;
	}


	public void setRestApiUrl(URL restApiUrl) {
		this.restApiUrl = restApiUrl;
	}


	public void setStreamingApiUrl(String streamingApiUrl) {
		this.streamingApiUrl = streamingApiUrl;
	}


	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}


	public void setMainAccountId(Integer mainAccountId) {
		this.mainAccountId = mainAccountId;
	}


	public String getFetchCandlesRegex() {
		return fetchCandlesRegex;
	}


	public void setFetchCandlesRegex(String fetchCandlesRegex) {
		this.fetchCandlesRegex = fetchCandlesRegex;
	}


}
