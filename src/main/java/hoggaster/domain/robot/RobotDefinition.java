package hoggaster.domain.robot;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.orders.OrderSide;
import hoggaster.rules.conditions.Condition;
import org.springframework.data.annotation.PersistenceConstructor;

import java.util.Set;
import java.util.UUID;

public class RobotDefinition {

    private final String id;

    public final String name;
    public final CurrencyPair currencyPair;
    public final OrderSide orderSide;
    private final Set<Condition> enterConditions;
    private final Set<Condition> exitConditions;


    public RobotDefinition(String name, CurrencyPair currencyPair, OrderSide orderSide) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.currencyPair = currencyPair;
        this.orderSide = orderSide;
        this.enterConditions = Sets.newHashSet();
        this.exitConditions = Sets.newHashSet();
    }

    @PersistenceConstructor
    public RobotDefinition(String id, String name, CurrencyPair currencyPair, OrderSide orderSide, Set<Condition> enterConditions, Set<Condition> exitConditions) {
        this.id = id;
        this.name = name;
        this.currencyPair = currencyPair;
        this.orderSide = orderSide;
        this.enterConditions = enterConditions;
        this.exitConditions = exitConditions;
    }

    public Set<Condition> getEnterConditions() {
        return enterConditions;
    }

    public void addEnterTradeCondition(Condition c) {
        Preconditions.checkNotNull(c);
        enterConditions.add(c);
    }

    public String getId() {
        return id;
    }


    public void addExitTradeCondition(Condition c) {
        Preconditions.checkNotNull(c);
        exitConditions.add(c);
    }

    public Set<Condition> getExitConditions() {
        return exitConditions;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("currencyPair", currencyPair)
                .add("enterConditions", enterConditions)
                .add("exitConditions", exitConditions)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RobotDefinition that = (RobotDefinition) o;
        return Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
