package hoggaster.web.vaadin.views.user.trades;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.validator.LongRangeValidator;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.depots.DbDepot;
import hoggaster.domain.orders.OrderSide;
import hoggaster.domain.orders.OrderType;
import hoggaster.domain.prices.Price;
import hoggaster.web.vaadin.GuiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.viritin.fields.EnumSelect;
import org.vaadin.viritin.fields.TypedSelect;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MFormLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;
import reactor.bus.EventBus;
import reactor.bus.registry.Registration;
import reactor.bus.selector.Selectors;

import java.util.Collection;

/**
 * Form for add/edit a trade. Use the stuff from viritn https://github.com/viritin/viritin
 * Created by svante2 on 2016-03-22.
 */
public class TradeForm extends AbstractForm<TradeForm.FormTrade> {

    private static final Logger LOG = LoggerFactory.getLogger(TradeForm.class);

    final EnumSelect<CurrencyPair> instrument = (EnumSelect<CurrencyPair>) new EnumSelect<CurrencyPair>("Instrument").withNullSelection(false)
            .withReadOnly(false);

    final TypedSelect<OrderSide> side = new EnumSelect<>().withSelectType(OptionGroup.class).withNullSelection(false)
            .withStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);

    final TypedSelect<OrderType> orderType = new EnumSelect<>().withSelectType(OptionGroup.class).withNullSelection(false)
            .withStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);

    final ComboBox depot = new ComboBox("Depot");

    final TextField units = new TextField("Units to order");

    final TextField price = new TextField("Last market price");

    private final boolean editMode;

    private final EventBus pricesEventBus;

    private Registration registration = null;

    private Long lastPriceUpdate = System.currentTimeMillis();

    public TradeForm(EventBus pricesEventBus, Collection<DbDepot> depots, FormTrade trade) {
        this(pricesEventBus, depots, trade, true);
    }

    public TradeForm(EventBus pricesEventBus, Collection<DbDepot> depots) {
        this(pricesEventBus, depots, new FormTrade(), false);
    }

    private TradeForm(EventBus pricesEventBus, Collection<DbDepot> depots, FormTrade trade, boolean editMode) {
        this.pricesEventBus = pricesEventBus;
        this.editMode = editMode;
        setEntity(trade);

        units.setConverter(Long.class);
        units.setValue("0");
        units.addValidator(new LongRangeValidator("Invalid value", 1L, 1_000_000L));

        price.setConverter(String.class);
        price.setEnabled(false);
        price.setValue("0.0");
        price.addStyleName("pushbox");

        side.setCaption("Buy or Sell");
        side.selectFirst();

        orderType.setCaption("Type of order");
        orderType.selectFirst();
        orderType.setEnabled(false);

        BeanItemContainer<DbDepot> depotContainer = new BeanItemContainer<DbDepot>(DbDepot.class);
        depotContainer.addAll(depots);
        depot.setContainerDataSource(depotContainer);
        depot.setNullSelectionAllowed(false);
        depot.setTextInputAllowed(false);
        depot.setItemCaptionPropertyId("name");
        depot.setInputPrompt("Please select a depot for this order");

        instrument.setSizeFull();
        instrument.addMValueChangeListener(v -> {
            if(getUI() != null) { //reset the price when we change instrument
                GuiUtils.setAndPushDoubleField(getUI(), price, 0.0, price.getValue() != null ? Double.valueOf(price.getValue()) : 0.0);
            }
            if(registration != null) registration.cancel();
            registration = pricesEventBus.on(Selectors.$("prices."+v.getValue()), e -> {
                if(System.currentTimeMillis() - lastPriceUpdate < 2500) {
                    return;
                }
                LOG.info("New price: {}", ((Price)e.getData()).ask);
                if(getUI() != null) {
                    GuiUtils.setAndPushDoubleField(getUI(), price, ((Price) e.getData()).ask.doubleValue(), Double.valueOf(price.getValue() != null ? price.getValue() : "0"));
                } else {
                    registration.cancel();
                }
                lastPriceUpdate = System.currentTimeMillis();
            });
        });
        instrument.selectFirst();
    }

    @Override
    protected Component createContent() {
        instrument.setOptions(CurrencyPair.values());
        instrument.selectFirst();

        side.setOptions(OrderSide.values());

        return new MVerticalLayout(
                new MFormLayout(
                        depot,
                        instrument,
                        side,
                        orderType,
                        units,
                        price).withWidth(""),
                getToolbar()).withWidth("");
    }

    public static class FormTrade {

        private DbDepot depot;

        private CurrencyPair instrument;

        private OrderSide side;

        private Long units;

        private String price;

        private OrderType orderType;

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public Long getUnits() {
            return units;
        }

        public void setUnits(Long units) {
            this.units = units;
        }

        public OrderSide getSide() {
            return side;
        }

        public void setSide(OrderSide side) {
            this.side = side;
        }

        public CurrencyPair getInstrument() {
            return instrument;
        }

        public void setInstrument(CurrencyPair instrument) {
            this.instrument = instrument;
        }

        public OrderType getOrderType() {
            return orderType;
        }

        public void setOrderType(OrderType orderType) {
            this.orderType = orderType;
        }

        public DbDepot getDepot() {
            return depot;
        }

        public void setDepot(DbDepot depot) {
            this.depot = depot;
        }
    }
}
