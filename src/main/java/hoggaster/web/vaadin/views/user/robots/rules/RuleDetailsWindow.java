package hoggaster.web.vaadin.views.user.robots.rules;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import hoggaster.rules.Comparator;
import hoggaster.rules.MarketUpdateType;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.fields.TypedSelect;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import static com.vaadin.server.FontAwesome.CHECK_CIRCLE;
import static com.vaadin.ui.themes.ValoTheme.BUTTON_PRIMARY;

/**
 * Used to create/edit a single rule.
 *
 * <p>
 * Created by svante2 on 2016-11-03.
 */
public class RuleDetailsWindow extends Window {

    private final String[] indicatorNames = new String[]{"Ask", "Bid", "RSI", "Simple value", "SMA"};

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

        TypedSelect<String> firstIndicator = new TypedSelect<>(String.class).withSelectType(ComboBox.class).withCaption("First indicator").withNullSelectionAllowed(false).setOptions(indicatorNames);
        firstIndicator.selectFirst();
        firstIndicator.addMValueChangeListener(e -> {

        });
        TypedSelect<Comparator> comparator = new TypedSelect<>(Comparator.class).withSelectType(ComboBox.class).setOptions(Comparator.values()).withCaption("Comparator").withNullSelectionAllowed(false);
        comparator.selectFirst();

        TypedSelect<String> secondIndicator = new TypedSelect<>(String.class).withSelectType(ComboBox.class).withCaption("Second indicator").withNullSelectionAllowed(false).setOptions(indicatorNames);
        secondIndicator.selectFirst();

        conditions.add(firstIndicator, comparator, secondIndicator);
        content.add(conditions);

        //The Market update type
        TypedSelect<MarketUpdateType> marketUpdateTypeTypedSelect = new TypedSelect<>(MarketUpdateType.class).withSelectType(ComboBox.class).withCaption("Market update type").withNullSelectionAllowed(false).setOptions(MarketUpdateType.values());
        content.add(marketUpdateTypeTypedSelect);

        //Buttons at the bottom
        MHorizontalLayout buttons = new MHorizontalLayout().withSpacing(true).withFullWidth().withFullHeight();
        Button okButton = new MButton("Ok", event -> close()).withIcon(CHECK_CIRCLE).withStyleName(BUTTON_PRIMARY);

        buttons.add(okButton);
        content.add(buttons);

    }

}
