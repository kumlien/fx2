package hoggaster.oanda;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oanda")
public class OandaProperties {

	private Long fetchInterval;

	private String restApiUrl;

	private String streamingApiUrl;

	private String apiKey;
	
	private Integer mainAccountId;

	public Long getFetchInterval() {
		return fetchInterval;
	}


	public String getRestApiUrl() {
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


	public void setFetchInterval(Long fetchInterval) {
		this.fetchInterval = fetchInterval;
	}


	public void setRestApiUrl(String restApiUrl) {
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


}
