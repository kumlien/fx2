package hoggaster.domain.orders;

/**
 * Interface for order service, implemented by broker connections.
 */
public interface OrderService {

    //TODO remove Oanda
    CreateOrderResponse sendOrder(OrderRequest request);
}
