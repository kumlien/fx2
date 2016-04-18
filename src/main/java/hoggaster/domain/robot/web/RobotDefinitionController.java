package hoggaster.domain.robot.web;

import com.google.common.base.Preconditions;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.robot.RobotDefinition;
import hoggaster.domain.robot.RobotDefinitionRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("robotdefs")
public class RobotDefinitionController {

    private static final Logger LOG = LoggerFactory.getLogger(RobotDefinitionController.class);

    private final RobotDefinitionRepo repo;

    @Autowired
    public RobotDefinitionController(RobotDefinitionRepo repo) {
        this.repo = repo;
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<RobotDefinition> getByDepotId(@RequestParam("depotId") String depotId) {
        List<RobotDefinition> all = repo.findByDepotId(depotId);
        LOG.info("Found these robot defs: {}", all);
        return all;
    }


    @RequestMapping(method = RequestMethod.POST)
    @ResponseStatus(CREATED)
    public RobotDefinition create(@RequestParam("name") String name, @RequestParam("instrument") CurrencyPair currencyPair, @RequestParam("depotId") String depotId) {
        RobotDefinition def = new RobotDefinition(name, currencyPair, depotId);
        repo.save(def);
        return def;
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    public RobotDefinition update(@PathVariable("id") String id, @Valid @RequestBody CreateRobotRequest req) {
        RobotDefinition def = repo.findOne(id);
        Preconditions.checkArgument(def != null, "No robot definition found with id " + id);
        repo.save(def);
        return def;
    }

    @RequestMapping(value = "{id}/buyConditions", method = RequestMethod.POST)
    public RobotDefinition addBuyConditions(@PathVariable("id") String id, @Valid @RequestBody ConditionRequest req) {
        RobotDefinition def = repo.findOne(id);
        Preconditions.checkArgument(def != null, "No robot definition found with id " + id);
        return def;
    }

}
