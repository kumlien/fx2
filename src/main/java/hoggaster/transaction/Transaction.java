package hoggaster.transaction;

import hoggaster.domain.CurrencyPair;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
public class Transaction {

    @Id
    private String id;

    private final String depotId;

    private final Instant time;

    private final TransactionType type;

    private final CurrencyPair currencyPair;

    private final Double quantity;

    private final Double price;

    private final Double commision;

    private final Double sum;

    private final String currency;

    @PersistenceConstructor
    public Transaction(String id, String depotId, Instant time, TransactionType type, CurrencyPair currencyPair, Double quantity, Double price, Double commision, Double sum, String currency) {
        this.id = id;
        this.depotId = depotId;
        this.time = time;
        this.type = type;
        this.currencyPair = currencyPair;
        this.quantity = quantity;
        this.price = price;
        this.commision = commision;
        this.sum = sum;
        this.currency = currency;
    }

    public Transaction(String depotId, Instant time, TransactionType type, CurrencyPair currencyPair, Double quantity, Double price, Double commision, Double sum, String currency) {
        this.depotId = depotId;
        this.time = time;
        this.type = type;
        this.currencyPair = currencyPair;
        this.quantity = quantity;
        this.price = price;
        this.commision = commision;
        this.sum = sum;
        this.currency = currency;
    }
}
