package hoggaster.domain.robot;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import hoggaster.domain.CurrencyPair;
import hoggaster.rules.conditions.Condition;

import java.util.Set;
import java.util.UUID;

public class RobotDefinition {

    private String id;

    public final String name;
    public final CurrencyPair currencyPair;
    private final Set<Condition> enterConditions;
    private final Set<Condition> exitConditions;


    public RobotDefinition(String name, CurrencyPair currencyPair) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.currencyPair = currencyPair;
        this.enterConditions = Sets.newHashSet();
        this.exitConditions = Sets.newHashSet();
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

}
