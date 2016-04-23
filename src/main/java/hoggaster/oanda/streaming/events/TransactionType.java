package hoggaster.oanda.streaming.events;

/**
 * Created by svante2 on 2016-04-15.
 */
public enum TransactionType {
    MARKET_ORDER_CREATE,
    STOP_ORDER_CREATE,
    LIMIT_ORDER_CREATE,
    MARKET_IF_TOUCHED_ORDER_CREATE,
    ORDER_UPDATE,
    ORDER_CANCEL,
    ORDER_FILLED,
    TRADE_UPDATE,
    TRADE_CLOSE,
    MIGRATE_TRADE_CLOSE,
    MIGRATE_TRADE_OPEN,
    TAKE_PROFIT_FILLED,
    STOP_LOSS_FILLED,
    TRAILING_STOP_FILLED,
    MARGIN_CALL_ENTER,
    MARGIN_CALL_EXIT,
    MARGIN_CLOSEOUT,
    SET_MARGIN_RATE,
    TRANSFER_FUNDS,
    DAILY_INTEREST,
    FEE
}