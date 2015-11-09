package hoggaster.transaction;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by svante.kumlien on 09.11.15.
 */
@Document
public class Order {

    @Id
    private String id;

    public String depotId;
}
