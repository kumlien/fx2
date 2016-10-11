package hoggaster.web.vaadin.views.user.robots;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.robot.RobotDefinition;

/**
 * Created by svante2 on 2016-04-24.
 */
public class UIRobot {

    private String id;

    private String name;

    private CurrencyPair instrument;

    private DbDepot dbDepot;

    public UIRobot(RobotDefinition robotDefinition, DbDepot dbDepot) {
        this.name = robotDefinition.name;
        this.instrument = robotDefinition.currencyPair;
        this.id = robotDefinition.getId();
        this.dbDepot = dbDepot;
    }

    public UIRobot() {

    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public CurrencyPair getInstrument() {
        return instrument;
    }

    public void setInstrument(CurrencyPair instrument) {
        this.instrument = instrument;
    }

    public DbDepot getDbDepot() {
        return dbDepot;
    }

    public void setDbDepot(DbDepot dbDepot) {
        this.dbDepot = dbDepot;
    }

    public String getDepotName() {
        return dbDepot.name;
    }

    public String getId() {
        return id;
    }

}
