package hoggaster.domain.trades.web;

import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.depots.DepotService;
import hoggaster.domain.depots.web.DepotNotFoundException;
import hoggaster.domain.trades.Trade;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by svante2 on 2015-11-26.
 */
@RestController
@RequestMapping("trades")
public class TradesController {

    private final DepotService depotService;

    private final BrokerConnection brokerConnection;

    @Autowired
    public TradesController(DepotService depotService, @Qualifier("OandaBrokerConnection") BrokerConnection brokerConnection) {
        this.depotService = depotService;
        this.brokerConnection = brokerConnection;
    }


    @RequestMapping(method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Get all open trades for the specified depotId, directly from the broker right now" )
    public Collection<Trade> getAll(@RequestParam("depotId") String depotId) {
        DbDepot dbDepot = depotService.findDepotById(depotId);
        if(dbDepot == null) {
            throw new DepotNotFoundException("No depot found in our db with id " + depotId);
        }
        return brokerConnection.getOpenTrades(depotId, dbDepot.brokerId);
    }

    @RequestMapping(value = "{tradeId}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Close the trade with the specified id" )
    public void closeTrade(@RequestParam("depotId") String depotId, @PathVariable("tradeId") String tradeId) {
        DbDepot dbDepot = depotService.findDepotById(depotId);
        brokerConnection.closeTrade(depotId, dbDepot.brokerId, tradeId);
    }

    @RequestMapping(value = "{tradeId}", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Get a specific trade for the specified depotId, directly from the broker right now" )
    public Trade getById(@RequestParam("depotId") String depotId, @PathVariable("tradeId") String tradeId) {
        DbDepot dbDepot = depotService.findDepotById(depotId);
        Optional<Trade> trade = brokerConnection.getTrade(depotId, dbDepot.brokerId, tradeId);
        if(trade.isPresent()) {
            return trade.get();
        }
        throw new TradeNotFoundException("No trade found with id " + tradeId);
    }
}
