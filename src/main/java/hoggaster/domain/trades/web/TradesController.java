package hoggaster.domain.trades.web;

import hoggaster.domain.brokers.BrokerConnection;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.depots.DepotService;
import hoggaster.domain.trades.Trade;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

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


    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("Send a new order request to oanda for the specified depotId" )
    public Collection<Trade> getAll(@RequestParam("depotId") String depotId) {
        DbDepot dbDepot = depotService.findDepotById(depotId);
        return brokerConnection.getOpenTrades(depotId, dbDepot.brokerId);
    }
}
