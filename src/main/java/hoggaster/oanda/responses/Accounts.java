package hoggaster.oanda.responses;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Accounts {

	private final List<OandaAccount> accounts;
	
	@JsonCreator
	protected Accounts(@JsonProperty(value="accounts") List<OandaAccount> accounts) {
		this.accounts = accounts;
	}

	public List<OandaAccount> getAccounts() {
		return accounts;
	}

	@Override
	public String toString() {
		return "Accounts [accounts=" + accounts + "]";
	}
}
