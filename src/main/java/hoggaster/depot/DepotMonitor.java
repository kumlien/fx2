package hoggaster.depot;

/**
 * Created by svante on 15-09-27.
 */
public interface DepotMonitor {

    Depot syncDepot(Depot depot);

    void checkDepotMargin(Depot depot, boolean doSync);
}
