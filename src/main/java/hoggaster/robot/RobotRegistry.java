package hoggaster.robot;

import com.google.common.base.Preconditions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for loaded robots.
 */
@Service
public class RobotRegistry {

    //stoopid registry for now
    private final Map<String, Robot> robots = new ConcurrentHashMap<String, Robot>();

    public List<Robot> getAllKnownRobots() {
        return new ArrayList<Robot>(robots.values());
    }

    public Robot getById(String robotId) {
        return robots.get(robotId);
    }

    public void add(Robot robot) {
        Preconditions.checkArgument(!robots.containsKey(robot.id), "There is already a robot with id " + robot.id + " in the registry");
        this.robots.put(robot.id, robot);
    }

    public List<Robot> findByStatus(RobotStatus... statuses) {
        return null;
    }

}
