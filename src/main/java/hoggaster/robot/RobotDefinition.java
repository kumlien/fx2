package hoggaster.robot;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import hoggaster.domain.Instrument;
import hoggaster.rules.Condition;
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
    public final Instrument instrument;
    private final Set<Condition> buyConditions;
    private final Set<Condition> sellConditions;
    private final String depotId;

    @PersistenceConstructor
    RobotDefinition(String id, String name, Instrument instrument, Set<Condition> buyConditions, Set<Condition> sellConditions, String depotId) {
        this.id = id;
        this.name = name;
        this.instrument = instrument;
        this.buyConditions = buyConditions;
        this.sellConditions = sellConditions;
        this.depotId = depotId;
    }

    public RobotDefinition(String name, Instrument instrument, String depotId) {
        this.name = name;
        this.instrument = instrument;
        this.depotId = depotId;
        this.buyConditions = Sets.newHashSet();
        this.sellConditions = Sets.newHashSet();
    }

    public Set<Condition> getBuyConditions() {
        return buyConditions;
    }

    public void addBuyCondition(Condition c) {
        Preconditions.checkNotNull(c);
        buyConditions.add(c);
    }

    public String getId() {
        return id;
    }

    public void addSellCondition(Condition c) {
        Preconditions.checkNotNull(c);
        sellConditions.add(c);
    }

    public Set<Condition> getSellConditions() {
        return sellConditions;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("RobotDefinition [id=").append(id).append(", name=")
                .append(name).append(", instrument=").append(instrument)
                .append(", buyConditions=").append(buyConditions)
                .append(", sellConditions=").append(sellConditions).append("]");
        return builder.toString();
    }

    public String getDepotId() {
        return depotId;
    }

}
