package hoggaster.domain.trades;

import com.google.common.base.Preconditions;
import hoggaster.domain.CurrencyPair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * @author svante
 */
@Service
public class TradeServiceImpl implements TradeService {

    private final TradeRepo tradeRepo;

    @Autowired
    public TradeServiceImpl(TradeRepo tradeRepo) {
        this.tradeRepo = tradeRepo;
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
}
