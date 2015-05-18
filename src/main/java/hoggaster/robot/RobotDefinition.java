package hoggaster.robot;

import hoggaster.domain.Instrument;
import hoggaster.rules.Condition;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

@Document
public class RobotDefinition {

	@Id
	private String id;
	public final String name;
	public final Instrument instrument;
	private final Set<Condition> buyConditions;
	private final Set<Condition> sellConditions;

	@PersistenceConstructor
	RobotDefinition(String id, String name, Instrument instrument, Set<Condition> buyConditions, Set<Condition> sellConditions) {
		this.id = id;
		this.name = name;
		this.instrument = instrument;
		this.buyConditions = buyConditions;
		this.sellConditions = sellConditions;
	}

	public RobotDefinition(String name, Instrument instrument) {
		this.name = name;
		this.instrument = instrument;
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
}
