package hoggaster.domain.trades;

import com.google.common.base.Preconditions;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.brokers.Broker;
import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.depots.DepotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.MathContext;
import java.util.Collection;

import static hoggaster.domain.trades.Trade.TradeBuilder.aTrade;
import static hoggaster.domain.trades.TradeStatus.CLOSED;
import static reactor.Environment.workDispatcher;

/**
 * @author svante
 */
@Service
public class TradeServiceImpl implements TradeService {

    private final TradeRepo tradeRepo;

    private final BrokerConnection brokerConnection;

    private final DepotService depotService;

    @Autowired
    public TradeServiceImpl(TradeRepo tradeRepo, @Qualifier("OandaBrokerConnection") BrokerConnection brokerConnection, DepotService depotService) {
        this.tradeRepo = tradeRepo;
        this.brokerConnection = brokerConnection;
        this.depotService = depotService;
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
        return tradeRepo.findByDepotIdAndStatus(depotId, CLOSED);
    }


    //Calculate some stuff on the trade
    //Move the trade to collection with historic trades
    //Update the depot afterwards
    @Override
    public CloseTradeResponse closeTrade(Trade trade, String brokerAccountId) {
        CloseTradeResponse closeTradeResponse = brokerConnection.closeTrade(trade, brokerAccountId);
        Trade tradeToSave = aTrade()
                .withBroker(Broker.OANDA)
                .withBrokerId(trade.brokerId)
                .withClosePrice(closeTradeResponse.price)
                .withCloseTime(closeTradeResponse.time)
                .withDepotId(trade.depotId)
                .withInstrument(trade.instrument)
                .withTotalGain(closeTradeResponse.profit)
                .withGainPerUnit(closeTradeResponse.price.divide(trade.units, MathContext.DECIMAL32))
                .withOpenPrice(trade.openPrice)
                .withOpenTime(trade.openTime)
                .withStatus(CLOSED)
                .withStopLoss(trade.stopLoss)
                .withTakeProfit(trade.takeProfit)
                .withTrailingAmount(trade.trailingAmount)
                .withTrailingStop(trade.trailingStop)
                .build();
        workDispatcher().dispatch(tradeToSave, t -> {
                    tradeRepo.save(t);
                },
                error -> {
                });

        depotService.syncDepotAsync(trade.depotId);
        return closeTradeResponse;
    }
}
