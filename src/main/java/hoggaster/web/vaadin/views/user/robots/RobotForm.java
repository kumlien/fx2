package hoggaster.web.vaadin.views.user.robots;

import com.google.common.collect.Lists;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
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
    private final BeanItemContainer<DbDepot> depotContainer = new BeanItemContainer<>(DbDepot.class);
    private final ComboBox dbDepot = new ComboBox("Depot", depotContainer);

    public RobotForm(Collection<DbDepot> dbDepots) {
        setModalWindowTitle("Add new robot");
        setEntity(new UIRobot());
        instrument.selectFirst();
        setupDepotCombo(dbDepots);
        setSizeUndefined();
    }

    public RobotForm(UIRobot robot) {
        setModalWindowTitle("Edit " + robot.getName());
        setEntity(robot);
        setupDepotCombo(Lists.newArrayList(robot.getDbDepot()));
    }

    @Override
    protected Component createContent() {
        getSaveButton().setIcon(FontAwesome.SAVE);
        getResetButton().setIcon(FontAwesome.CLOSE);
        return new MVerticalLayout(
                new MFormLayout(name, instrument, dbDepot).withWidth("").withSizeUndefined(),
                getToolbar()
        ).withWidth("").withSizeUndefined();
    }

    private void setupDepotCombo(Collection<DbDepot> dbDepots) {
        depotContainer.addAll(dbDepots);
        dbDepot.setNullSelectionAllowed(false);
        dbDepot.setTextInputAllowed(false);
        dbDepot.setItemCaptionPropertyId("name");
        dbDepot.setInputPrompt("Please select a depot for this robot");
        dbDepot.select(depotContainer.firstItemId());
    }


}
