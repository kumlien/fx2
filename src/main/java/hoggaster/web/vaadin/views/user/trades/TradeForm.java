package hoggaster.web.vaadin.views.user.trades;

import com.vaadin.data.validator.IntegerRangeValidator;
import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextField;
import com.vaadin.ui.themes.ValoTheme;
import hoggaster.domain.CurrencyPair;
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

    final TextField units = new TextField("Units");

    final TextField price = new TextField("Price");

    private final boolean editMode;

    private final EventBus pricesEventBus;

    private Registration registration = null;

    private Long lastPriceUpdate = System.currentTimeMillis();

    public TradeForm(EventBus pricesEventBus, FormTrade trade) {
        this(pricesEventBus, trade, true);
    }

    public TradeForm(EventBus pricesEventBus) {
        this(pricesEventBus, new FormTrade(), false);
    }

    private TradeForm(EventBus pricesEventBus, FormTrade trade, boolean editMode) {
        this.pricesEventBus = pricesEventBus;
        this.editMode = editMode;
        setEntity(trade);

        units.setConverter(Integer.class);
        units.setValue("0");
        units.addValidator(new IntegerRangeValidator("Invalid value", 0, 1_000_000));

        price.setConverter(String.class);
        price.setEnabled(false);
        price.setValue("0.0");

        side.setCaption("Buy or Sell");
        side.selectFirst();

        orderType.setCaption("Type of order");
        orderType.selectFirst();
        orderType.setEnabled(false);

        instrument.setSizeFull();
        instrument.addMValueChangeListener(v -> {
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
            LOG.info("Value changed -> {}", v);
        });
        instrument.selectFirst();


    }

    @Override
    protected Component createContent() {
        instrument.setOptions(CurrencyPair.values());
        side.setOptions(OrderSide.values());

        return new MVerticalLayout(
                new MFormLayout(
                        instrument,
                        side,
                        orderType,
                        units,
                        price).withWidth(""),
                getToolbar()).withWidth("");
    }

    public static class FormTrade {

        private CurrencyPair instrument;

        private OrderSide side;

        private Integer units;

        private String price;

        private OrderType orderType;

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public Integer getUnits() {
            return units;
        }

        public void setUnits(Integer units) {
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
    }
}
