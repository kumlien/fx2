package hoggaster.robot;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import hoggaster.domain.CurrencyPair;
import hoggaster.rules.conditions.Condition;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document
public class RobotDefinition {

    @Id
    private String id;

    @Indexed(unique = true)
    public final String name;
    public final CurrencyPair currencyPair;
    private final Set<Condition> enterConditions;
    private final Set<Condition> exitConditions;
    private final String depotId;

    @PersistenceConstructor
    RobotDefinition(String id, String name, CurrencyPair currencyPair, Set<Condition> buyConditions, Set<Condition> sellConditions, String depotId) {
        this.id = id;
        this.name = name;
        this.currencyPair = currencyPair;
        this.enterConditions = buyConditions;
        this.exitConditions = sellConditions;
        this.depotId = depotId;
    }

    public RobotDefinition(String name, CurrencyPair currencyPair, String depotId) {
        this.name = name;
        this.currencyPair = currencyPair;
        this.depotId = depotId;
        this.enterConditions = Sets.newHashSet();
        this.exitConditions = Sets.newHashSet();
    }

    public Set<Condition> getEnterConditions() {
        return enterConditions;
    }

    public void addBuyCondition(Condition c) {
        Preconditions.checkNotNull(c);
        enterConditions.add(c);
    }

    public String getId() {
        return id;
    }

    public void addSellCondition(Condition c) {
        Preconditions.checkNotNull(c);
        exitConditions.add(c);
    }

    public Set<Condition> getExitConditions() {
        return exitConditions;
    }

    public String getDepotId() {
        return depotId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("currencyPair", currencyPair)
                .add("enterConditions", enterConditions)
                .add("exitConditions", exitConditions)
                .add("depotId", depotId)
                .toString();
    }
}
