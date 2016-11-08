package hoggaster.web.vaadin.views.user.depots;

import hoggaster.domain.brokers.Broker;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.depots.Depot;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Component;
import org.vaadin.viritin.fields.EnumSelect;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.fields.TypedSelect;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.grid.StringPropertyValueGenerator;
import org.vaadin.viritin.layouts.MFormLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;
import java.util.Arrays;

/**
 * Created by svante on 2016-11-08.
 */
public class DepotForm extends AbstractForm<DepotForm.FormDepot> {

    MTextField name = new MTextField("Name of the depot").withNullRepresentation("Name").withRequired(true);

    EnumSelect<Broker> broker = (EnumSelect<Broker>) new EnumSelect<Broker>("Broker").setOptions(Broker.values()).withNullSelectionAllowed(false);

    EnumSelect<DbDepot.Type> type = (EnumSelect<DbDepot.Type>) new EnumSelect<DbDepot.Type>("Depot type").setOptions(DbDepot.Type.values()).withNullSelectionAllowed(false);

    MTextField brokerId = new MTextField("Broker depot id").withNullRepresentation("Broker id").withRequired(true);


    private DepotForm(DbDepot depot) {
        setModalWindowTitle("Edit " + depot.getName());
        setEntity(new FormDepot(depot.name, depot.broker, depot.type, depot.brokerId));
    }

    public DepotForm() {
        setEntity(new FormDepot());
        setModalWindowTitle("Connect a new depot");
        broker.selectFirst();
        type.selectFirst();
    }

    public static DepotForm withDepot(DbDepot depot) {
        return new DepotForm(depot);
    }

    @Override protected Component createContent() {
        brokerId.setDescription("This is the id of the depot you want to connect to in the system of the broker (Oanda for now)");
        getSaveButton().setIcon(FontAwesome.SAVE);
        getResetButton().setIcon(FontAwesome.CLOSE);
        return new MVerticalLayout(
                new MFormLayout(name, broker, brokerId, type).withWidth("").withSizeUndefined(),
                getToolbar()
        ).withWidth("").withSizeUndefined();
    }

    public class FormDepot {

        private String name;

        private Broker broker;

        private DbDepot.Type type;

        private String brokerId;

        FormDepot(String name, Broker broker, DbDepot.Type type, String brokerId) {
            this.name = name;
            this.broker = broker;
            this.type = type;
            this.brokerId = brokerId;
        }

        public FormDepot() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Broker getBroker() {
            return broker;
        }

        public void setBroker(Broker broker) {
            this.broker = broker;
        }

        public DbDepot.Type getType() {
            return type;
        }

        public void setType(DbDepot.Type type) {
            this.type = type;
        }

        public String getBrokerId() {
            return brokerId;
        }

        public void setBrokerId(String brokerId) {
            this.brokerId = brokerId;
        }
    }
}
