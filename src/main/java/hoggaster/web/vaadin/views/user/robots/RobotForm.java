package hoggaster.web.vaadin.views.user.robots;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.depots.DbDepot;
import org.vaadin.viritin.fields.EnumSelect;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MFormLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.util.Collection;

/**
 * Created by svante2 on 2016-09-09.
 */
public class RobotForm extends AbstractForm<UIRobot> {

    private final TextField name = new MTextField("name").withCaption("The name of the robot").withRequired(true).withRequiredError("You must provide a name for the robot");
    private final EnumSelect<CurrencyPair> instrument = (EnumSelect<CurrencyPair>) new EnumSelect<CurrencyPair>("Instrument").withNullSelection(false).setOptions(CurrencyPair.values());
    final ComboBox dbDepot = new ComboBox("Depot");

    public RobotForm(Collection<DbDepot> dbDepots) {
        setCaption("Add new robot");
        setEntity(new UIRobot());
        instrument.selectFirst();


        BeanItemContainer<DbDepot> depotContainer = new BeanItemContainer<DbDepot>(DbDepot.class);
        depotContainer.addAll(dbDepots);
        dbDepot.setContainerDataSource(depotContainer);
        dbDepot.setNullSelectionAllowed(false);
        dbDepot.setTextInputAllowed(false);
        dbDepot.setItemCaptionPropertyId("name");
        dbDepot.setInputPrompt("Please select a depot for this robot");
        dbDepot.select(dbDepots.iterator().next());


        setSizeUndefined();

    }

    @Override
    protected Component createContent() {
        setCaption("Add new robot (create content)");
        return new MVerticalLayout(
                new MFormLayout(name, instrument, dbDepot).withWidth("").withSizeUndefined(),
                getToolbar()
        ).withWidth("").withSizeUndefined();
    }
}
