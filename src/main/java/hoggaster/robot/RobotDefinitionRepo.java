package hoggaster.robot;

import hoggaster.domain.robot.RobotDefinition;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RobotDefinitionRepo extends MongoRepository<RobotDefinition, String> {

    List<RobotDefinition> findByDepotId(String depotId);

    List<RobotDefinition> findByUserId(String id);
}
