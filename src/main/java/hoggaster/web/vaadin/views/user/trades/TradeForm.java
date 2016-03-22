package hoggaster.web.vaadin.views.user.trades;

import com.vaadin.ui.Component;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.themes.ValoTheme;
import hoggaster.domain.CurrencyPair;
import hoggaster.domain.orders.OrderSide;
import org.vaadin.viritin.fields.EnumSelect;
import org.vaadin.viritin.fields.TypedSelect;
import org.vaadin.viritin.form.AbstractForm;
import org.vaadin.viritin.layouts.MFormLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.math.BigDecimal;

/**
 * Form for add/edit a trade. Use the stuff from viritn https://github.com/viritin/viritin
 * Created by svante2 on 2016-03-22.
 */
public class TradeForm extends AbstractForm<TradeForm.FormTrade> {

    EnumSelect<CurrencyPair> instrument = (EnumSelect<CurrencyPair>) new EnumSelect<CurrencyPair>("Instrument").withNullSelection(false).withReadOnly(false);

    TypedSelect<OrderSide> side = new EnumSelect<>().withSelectType(OptionGroup.class).withNullSelection(false).withStyleName(ValoTheme.OPTIONGROUP_HORIZONTAL);



    private final boolean editMode;

    public TradeForm(UITrade trade) {
        editMode=true;
    }

    public TradeForm() {
        editMode = false;
        setEntity(new FormTrade());
    }

    @Override
    protected Component createContent() {
        instrument.setOptions(CurrencyPair.values());
        side.setOptions(OrderSide.values());

        return new MVerticalLayout(
                new MFormLayout(
                        instrument,
                        side
                ).withWidth(""),
                getToolbar()
        ).withWidth("");
    }

    public static class FormTrade {

        private CurrencyPair instrument;

        private OrderSide side;

        private BigDecimal units;

        private BigDecimal price;

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getUnits() {
            return units;
        }

        public void setUnits(BigDecimal units) {
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
    }
}
