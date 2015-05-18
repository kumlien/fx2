package hoggaster.robot;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface RobotDefinitionRepo extends MongoRepository<RobotDefinition, String>{

}
