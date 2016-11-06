package hoggaster.web.vaadin.views.user.robots.rules;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Window;
import com.vaadin.ui.themes.ValoTheme;
import hoggaster.rules.Comparator;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.fields.TypedSelect;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

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

        MTextField ruleName = new MTextField("Name:").withNullRepresentation("the name of your rule").withFullWidth();
        ruleName.setNullSettingAllowed(false);
        content.addComponent(ruleName);

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

        MHorizontalLayout buttons = new MHorizontalLayout().withSpacing(true).withFullWidth().withFullHeight();
        Button ok = new MButton("Ok", event -> close()).withIcon(FontAwesome.CHECK_CIRCLE).withStyleName(ValoTheme.BUTTON_PRIMARY);

        buttons.add(ok);
        content.add(buttons);

    }

}
