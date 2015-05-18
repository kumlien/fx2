package hoggaster.oanda.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class OandaAccount {
	

	public final String accountId;
	public final String accountName;
	public final String accountCurrency;
	public final String marginRate;
	
	@JsonCreator
	public OandaAccount(@JsonProperty(value="accountId") String accountId, @JsonProperty(value="accountName")String accountName,@JsonProperty(value="accountCurrency")String accountCurrency, @JsonProperty(value="marginRate")String marginRate) {
		this.accountId = accountId;
		this.accountName = accountName;
		this.accountCurrency = accountCurrency;
		this.marginRate = marginRate;
	}
	
	@Override
	public String toString() {
		return "Account [accountId=" + accountId + ", accountName="
				+ accountName + ", accountCurrency=" + accountCurrency
				+ ", marginRate=" + marginRate + "]";
	}
}
