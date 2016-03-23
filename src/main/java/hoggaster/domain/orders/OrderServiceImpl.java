package hoggaster.domain.orders;

import hoggaster.domain.brokers.BrokerConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * Created by svante.kumlien on 23.03.16.
 */
@Service
public class OrderServiceImpl {

    private final BrokerConnection broker;

    @Autowired
    public OrderServiceImpl(@Qualifier("OandaBrokerConnection") BrokerConnection broker) {
        this.broker = broker;
    }

    public OrderResponse sendOrder(OrderRequest request) {
        return broker.sendOrder(request);
    }
}
