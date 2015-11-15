package hoggaster.domain.transaction;

import hoggaster.domain.CurrencyPair;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document
public class Transaction {

    @Id
    private String id;

    public final String depotId;

    public final Instant time;

    public final TransactionType type;

    public final CurrencyPair currencyPair;

    public final BigDecimal quantity;

    public final BigDecimal price;

    public final BigDecimal commision;

    public final BigDecimal sum;

    public final String currency;

    @PersistenceConstructor
    public Transaction(String id, String depotId, Instant time, TransactionType type, CurrencyPair currencyPair, BigDecimal quantity, BigDecimal price, BigDecimal commision, BigDecimal sum, String currency) {
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

    public Transaction(String depotId, Instant time, TransactionType type, CurrencyPair currencyPair, BigDecimal quantity, BigDecimal price, BigDecimal commision, BigDecimal sum, String currency) {
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

    @Override
    public String toString() {
        return "Transaction{" +
                "id='" + id + '\'' +
                ", depotId='" + depotId + '\'' +
                ", time=" + time +
                ", type=" + type +
                ", currencyPair=" + currencyPair +
                ", quantity=" + quantity +
                ", price=" + price +
                ", commision=" + commision +
                ", sum=" + sum +
                ", currency='" + currency + '\'' +
                '}';
    }
}
