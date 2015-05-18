package hoggaster.robot.web;

import hoggaster.robot.RobotDefinition;
import hoggaster.robot.RobotDefinitionRepo;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("robotdefs")
public class RobotDefinitionController {

	private final RobotDefinitionRepo repo;
	
	@Autowired
	public RobotDefinitionController(RobotDefinitionRepo repo) {
		this.repo = repo;
	}
	
	
	@RequestMapping(method=RequestMethod.POST)
	public RobotDefinition create(@Valid @RequestBody CreateRobotRequest req) {
		RobotDefinition def = new RobotDefinition(req.name, req.instrument);
		repo.save(def);
		return def;
	}
	
	@RequestMapping(value="{id}/buyConditions", method=RequestMethod.POST)
	public RobotDefinition addBuyConditions(@Valid @RequestBody ConditionRequest req){
		return null;
	}

}
