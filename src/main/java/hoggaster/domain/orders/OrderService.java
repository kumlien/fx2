package hoggaster.domain.orders;

/**
 * Interface for order service, implemented by broker connections.
 */
public interface OrderService {

    CreateOrderResponse sendOrder(OrderRequest request);
}
