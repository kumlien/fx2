package hoggaster.web.vaadin.views.user;

import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.ConverterFactory;
import hoggaster.domain.depots.DbDepot.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.viritin.fields.MTable;
import org.vaadin.viritin.label.Header;
import org.vaadin.viritin.layouts.MVerticalLayout;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.themes.ValoTheme;

import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.depots.DepotService;
import hoggaster.web.vaadin.views.UserForm.FormUser;

import java.util.Locale;

/**
 * Main view for displaying info for a specific user.
 *
 * Contains a top header with user info
 * Below is a tabbed pane with tabs for
 * <ul>
 *     <li>Depots</li>
 *     <li>Positions</li>
 *     <li>Trades</li>
 *     <li>Robots</li>
 * </ul>
 *
 * @author svante.kumlien
 */
@SpringView(name = UserView.VIEW_NAME)
public class UserView extends MVerticalLayout implements View {
    public static final String VIEW_NAME = "UserView";
    public static final String SESSION_ATTRIBUTE_SELECTED_USER = "SelectedUser";

    private final DepotService depotService;

    @Autowired
    public UserView(DepotService depotService) {
        this.depotService = depotService;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        FormUser user = (FormUser) getUI().getSession().getAttribute(SESSION_ATTRIBUTE_SELECTED_USER);


        Header header = new Header("Details for " + user.getFirstName() + " " + user.getLastName());
        header.setHeaderLevel(2);

        TabSheet tabSheet = new TabSheet();
        tabSheet.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabSheet.addStyleName(ValoTheme.TABSHEET_PADDED_TABBAR);
        tabSheet.addTab(createDepotsTab(), "Depots");
        tabSheet.addTab(createPositionsTab(), "Positions");
        tabSheet.addTab(createTradesTab(), "Trades");
        tabSheet.addTab(createRobotsTab(), "Robots");

        addComponents(header, tabSheet);
        expand(tabSheet);
    }

    private Component createRobotsTab() {
        MVerticalLayout tab = new MVerticalLayout();
        return tab;
    }

    private Component createTradesTab() {
        MVerticalLayout tab = new MVerticalLayout();
        return tab;
    }

    private Component createPositionsTab() {
        MVerticalLayout tab = new MVerticalLayout();
        return tab;
    }

    private MVerticalLayout createDepotsTab() {
        MVerticalLayout depotsTab = new MVerticalLayout();
        MTable<DbDepot> depotsTable = new MTable(DbDepot.class)
                .withCaption("Depots")
                .withProperties("name", "type", "balance", "currency", "marginRate", "marginAvailable","numberOfOpenTrades","realizedPl","unrealizedPl", "lastSynchronizedWithBroker")
                .withColumnHeaders("Name", "Type", "Balance", "Currency", "Margin rate", "Margin available", "Number of open trades", "Realized profit/loss", "Unrealized profit/loss", "Last synchronized with broker")
                .withFullWidth();
        ConverterFactory
        depotsTable.setConverter(Type.class);
        depotsTable.setConverter("type", new Converter<String, Type>() {
            @Override
            public Type convertToModel(String value, Class<? extends Type> targetType, Locale locale) throws ConversionException {
                return Type.valueOf(value.toUpperCase());
            }

            @Override
            public String convertToPresentation(Type value, Class<? extends String> targetType, Locale locale) throws ConversionException {
                return value.toString().toLowerCase();
            }

            @Override
            public Class<Type> getModelType() {
                return Type.class;
            }

            @Override
            public Class<String> getPresentationType() {
                return String.class;
            }
        });
        depotsTable.setBeans(depotService.findAll());
        depotsTab.addComponents(depotsTable);
        depotsTab.expand(depotsTable);
        return depotsTab;
    }
}
