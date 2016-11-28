package hoggaster.web.vaadin.views.user.robots.rules;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import hoggaster.rules.Comparator;
import hoggaster.rules.MarketUpdateType;
import hoggaster.rules.indicators.*;
import org.apache.commons.lang3.ArrayUtils;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.fields.TypedSelect;
import org.vaadin.viritin.fields.config.ComboBoxConfig;
import org.vaadin.viritin.fields.config.OptionGroupConfig;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.util.Optional;

import static com.vaadin.server.FontAwesome.CHECK_CIRCLE;
import static com.vaadin.ui.themes.ValoTheme.BUTTON_PRIMARY;

/**
 * Used to create/edit a single rule with two indicators.
 *
 * <p>
 * Created by svante2 on 2016-11-03.
 */
public class RuleDetailsWindow extends Window {

    private final String[] indicatorNames = new String[]{"Ask (Tick)", "Bid (Tick)","Candle close", "RSI", "Simple value", "SMA"};

    Indicator firstIndicator = null;
    Indicator secondIndicator = null;

    public RuleDetailsWindow() {
        super("Create new Rule");
        center();

        MVerticalLayout content = new MVerticalLayout();
        content.setMargin(true);
        setContent(content);

        //The name of the rule at the top
        MTextField ruleName = new MTextField("Name:").withNullRepresentation("the name of your rule").withFullWidth();
        ruleName.setNullSettingAllowed(false);
        content.addComponent(ruleName);

        //Indicators and comparator below the name
        MHorizontalLayout conditions = new MHorizontalLayout().withSpacing(true).withFullWidth();

        TypedSelect<String> firstIndicatorSelect = new TypedSelect<>(String.class).asComboBoxType(ComboBoxConfig.build().withTextInputAllowed(false)).withCaption("First indicator").withNullSelectionAllowed(false).setOptions(indicatorNames);
        firstIndicatorSelect.selectFirst();
        firstIndicatorSelect.addMValueChangeListener(e -> {
            Notification.show("Changed: " + e.getValue());
            if(indicatorNames[0].equals(e.getValue())) {
                firstIndicator = new CurrentAskIndicator();
            } else if(indicatorNames[1].equals(e.getValue())) {
                firstIndicator = new CurrentBidIndicator();
            }
        });

        TypedSelect<Comparator> comparator = new TypedSelect<>(Comparator.class).asComboBoxType(ComboBoxConfig.build().withTextInputAllowed(false)).setOptions(Comparator.values()).withCaption("Comparator").withNullSelectionAllowed(false);
        comparator.selectFirst();

        TypedSelect<String> secondIndicator = new TypedSelect<>(String.class).asComboBoxType(ComboBoxConfig.build().withTextInputAllowed(false)).withCaption("Second indicator").withNullSelectionAllowed(false).setOptions(indicatorNames);
        secondIndicator.selectFirst();

        conditions.add(firstIndicatorSelect, comparator, secondIndicator);
        content.add(conditions);

        //The Market update type (this needs to figured out based on the indicators!)

        //Buttons at the bottom
        MHorizontalLayout buttons = new MHorizontalLayout().withSpacing(true).withFullWidth().withFullHeight();
        Button okButton = new MButton("Ok", event -> close()).withIcon(CHECK_CIRCLE).withStyleName(BUTTON_PRIMARY);

        buttons.add(okButton);
        content.add(buttons);

    }

}
