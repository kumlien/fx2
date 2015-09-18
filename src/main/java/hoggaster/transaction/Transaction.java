package hoggaster.transaction;

import hoggaster.domain.Instrument;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document
public class Transaction {

    @Id
    private String id;

    private String depotId;

    private Instant time;

    private TransactionType type;

    private Instrument instrument;

    private Double quantity;

    private Double price;

    private Double commision;

    private Double sum;

    private String currency;

}
