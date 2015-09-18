package hoggaster.oanda.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Accounts {

    private final List<OandaAccount> accounts;

    @JsonCreator
    protected Accounts(@JsonProperty(value = "accounts") List<OandaAccount> accounts) {
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
