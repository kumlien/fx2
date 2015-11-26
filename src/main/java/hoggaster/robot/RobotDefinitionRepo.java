package hoggaster.robot;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RobotDefinitionRepo extends MongoRepository<RobotDefinition, String> {

    List<RobotDefinition> findByDepotId(String depotId);
}
