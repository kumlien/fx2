package hoggaster.web.vaadin.views.user.robots;

import hoggaster.domain.CurrencyPair;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.robot.RobotDefinition;
import hoggaster.rules.conditions.Condition;

import java.util.Set;

/**
 * Represents a robot, used in the vaadin ui
 *
 * Created by svante2 on 2016-04-24.
 */
public class UIRobot {

    private String id;

    private String name;

    private CurrencyPair instrument;

    private OrderSide orderSide;

    private DbDepot dbDepot;

    private RobotDefinition robotDefinition;
    private Set<Condition> exitConditions;
    private Set<Condition> enterConditions;

    public UIRobot(RobotDefinition robotDefinition, DbDepot dbDepot) {
        this.name = robotDefinition.name;
        this.instrument = robotDefinition.currencyPair;
        this.id = robotDefinition.getId();
        this.dbDepot = dbDepot;
        this.robotDefinition = robotDefinition;
        this.orderSide = robotDefinition.orderSide;
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

    public OrderSide getOrderSide() {
        return orderSide;
    }

    public void setOrderSide(OrderSide orderSide) {
        this.orderSide = orderSide;
    }

    public String getId() {
        return id;
    }

    public RobotDefinition getRobotDefinition() {
        return robotDefinition;
    }

    public Set<Condition> getExitConditions() {
        return exitConditions;
    }

    public void setExitConditions(Set<Condition> exitConditions) {
        this.exitConditions = exitConditions;
    }

    public Set<Condition> getEnterConditions() {
        return enterConditions;
    }

    public void setEnterConditions(Set<Condition> enterConditions) {
        this.enterConditions = enterConditions;
    }
}
