package hoggaster.domain.trades;

import com.google.common.base.Preconditions;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.brokers.BrokerConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * @author svante
 */
@Service
public class TradeServiceImpl implements TradeService {

    private final TradeRepo tradeRepo;

    private final BrokerConnection brokerConnection;

    @Autowired
    public TradeServiceImpl(TradeRepo tradeRepo, @Qualifier("OandaBrokerConnection") BrokerConnection brokerConnection) {
        this.tradeRepo = tradeRepo;
        this.brokerConnection = brokerConnection;
    }

    @Override
    public Trade saveNewTrade(Trade trade) {
        return tradeRepo.save(trade);
    }

    @Override
    public void updateTrade(Trade trade) {
        Preconditions.checkArgument(trade != null);
        Preconditions.checkArgument(StringUtils.hasText(trade.getId()), "The trade to update must have an id");
        tradeRepo.save(trade);
    }

    @Override
    public Trade findTradeById(String id) {
        Preconditions.checkArgument(StringUtils.hasText(id), "Please provide an id which contains some text");
        return tradeRepo.findOne(id);
    }

    @Override
    public Trade findTradeByBrokerId(String brokerId) {
        Preconditions.checkArgument(StringUtils.hasText(brokerId), "Please provide a brokerId which contains some text");
        return tradeRepo.findByBrokerId(brokerId);
    }

    @Override
    public Collection<Trade> getOpenTrades(String depotId) {
        Preconditions.checkArgument(StringUtils.hasText(depotId), "Please provide a depotId which contains some text");
        return tradeRepo.findByDepotIdAndStatus(depotId, TradeStatus.OPEN);
    }

    @Override
    public Collection<Trade> findByInstrumentAndRobotId(CurrencyPair instrument, String robotId) {
        Preconditions.checkNotNull(instrument, "The instrument must not be null");
        Preconditions.checkArgument(StringUtils.hasText(robotId), "The robotId must contain some text");
        return tradeRepo.findByInstrumentAndRobotId(instrument, robotId);
    }

    @Override
    public Collection<Trade> getClosedTrades(String depotId) {
        Preconditions.checkArgument(StringUtils.hasText(depotId), "Please provide a depotId which contains some text");
        return tradeRepo.findByDepotIdAndStatus(depotId, TradeStatus.CLOSED);
    }

    @Override
    public CloseTradeResponse closeTrade(Trade trade, String brokerId) {
        return null;
    }

    //Close the trade on the broker side, save the trade to the historic trade collection and sync the depot.
    @Override
    public CompletableFuture<CloseTradeResponse> closeTradeAsync(Trade trade, String brokerAccountId) {
        CloseTradeResponse closeTradeResponse = brokerConnection.closeTrade(trade, brokerAccountId);
        Trade tradeToSave = Trade.TradeBuilder.aTrade()
                                    .withBroker(Broker.OANDA)
                                    .withBrokerId(trade.brokerId)
                                    .withClosePrice(closeTradeResponse.price)
                                    .withCloseTime(closeTradeResponse.time)
                                    .withDepotId(trade.depotId)
                                    .withInstrument(trade.instrument)
                                    .withOpenPrice(trade.openPrice)
                                    .withOpenTime(trade.openTime)
                                    .withGainPerUnit(calculateGainPerUnit(trade, closeTradeResponse)) //TODO
                                    .withTotalGain(calculateTotalGain(trade, closeTradeResponse)) //TODO
                                    .withRobotId(trade.robotId)
                                    .withSide(trade.side)
                                    .withStatus(TradeStatus.CLOSED)
                                    .withStopLoss(trade.stopLoss)
                                    .withTakeProfit(trade.takeProfit)
                                    .withTrailingAmount(trade.trailingAmount)
                                    .withUnits(trade.units)
                                    .build();
        tradeRepo.save(tradeToSave);

        return null;
    }

    private BigDecimal calculateGainPerUnit(Trade trade, CloseTradeResponse closeTradeResponse) {
        return null;
    }

    private BigDecimal calculateTotalGain(Trade trade, CloseTradeResponse closeTradeResponse) {
        return null;
    }
}
