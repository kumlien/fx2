package hoggaster.robot;

import hoggaster.domain.Instrument;
import hoggaster.prices.Price;
import hoggaster.rules.Condition;
import hoggaster.rules.indicators.CandleStickGranularity;
import hoggaster.user.Depot;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * God knows what this is... 
 * Some kind of context for each new price/candle. Contains info needed by a {@link Condition} to evalute
 * if it's is positive or not.
 * 
 * Collection of instances which might come in handy for {@link Condition}s when evaluating. 
 * It also used as a way for a {@link Condition} to signal to the {@link Robot} that it's
 * evaluation was positive. 
 * 
 */
public class RobotExecutionContext {
	
	public final Price price;
	
	public final Depot depot;

	public final Instrument instrument;
	
	private final List<Condition> positiveBuyConditions = new ArrayList<Condition>();
	
	private final List<Condition> positiveSellConditions = new ArrayList<Condition>();
	
	private final MovingAverageService maService;
	
	public RobotExecutionContext(Price price, Depot depot, Instrument instrument, MovingAverageService maService) {
		Preconditions.checkNotNull(price);
		Preconditions.checkNotNull(depot);
		Preconditions.checkNotNull(instrument);
		Preconditions.checkNotNull(maService);
		this.price = price;
		this.depot = depot;
		this.instrument = instrument;
		this.maService = maService;
	}

	public void addBuyAction(Condition condition) {
		positiveBuyConditions.add(condition);
	}
	
	public void addSellAction(Condition condition) {
		positiveSellConditions.add(condition);
	}

	public Double getMovingAverage(CandleStickGranularity granularity, int numberOfDataPoints) {
		return maService.getMA(instrument, granularity, numberOfDataPoints);
	}

	public List<Condition> getPositiveBuyConditions() {
		return positiveBuyConditions;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("RobotExecutionContext [price=").append(price)
				.append(", depot=").append(depot).append(", instrument=")
				.append(instrument).append(", positiveConditions=")
				.append(positiveBuyConditions).append("]");
		return builder.toString();
	}

	public List<Condition> getPositiveSellConditions() {
		return positiveSellConditions;
	}

}
