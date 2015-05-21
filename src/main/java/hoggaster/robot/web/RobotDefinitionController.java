package hoggaster.robot.web;

import java.util.List;

import hoggaster.robot.RobotDefinition;
import hoggaster.robot.RobotDefinitionRepo;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Preconditions;

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
	public List<RobotDefinition> getAll() {
		List<RobotDefinition> all = repo.findAll();
		LOG.info("Found these robot defs: {}", all);
		return all;
	}
	
	
	@RequestMapping(method=RequestMethod.POST)
	public RobotDefinition create(@Valid @RequestBody CreateRobotRequest req) {
		RobotDefinition def = new RobotDefinition(req.name, req.instrument);
		repo.save(def);
		return def;
	}
	
	@RequestMapping(value="{id}", method=RequestMethod.PUT)
	public RobotDefinition update(@PathVariable("id") String id, @Valid @RequestBody CreateRobotRequest req) {
		RobotDefinition def = repo.findOne(id);
		Preconditions.checkArgument(def != null, "No robot definition found with id " + id);
		repo.save(def);
		return def;
	}
	
	@RequestMapping(value="{id}/buyConditions", method=RequestMethod.POST)
	public RobotDefinition addBuyConditions(@PathVariable("id") String id,  @Valid @RequestBody ConditionRequest req){
		RobotDefinition def = repo.findOne(id);
		Preconditions.checkArgument(def != null, "No robot definition found with id " + id);
		return def;
	}

}
