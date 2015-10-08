package hoggaster.depot;

/**
 * Created by svante on 15-09-27.
 */
public interface DepotMonitor {

    DbDepot syncDepot(DbDepot dbDepot);

    void checkDepotMargin(DbDepot dbDepot, boolean doSync);
}
