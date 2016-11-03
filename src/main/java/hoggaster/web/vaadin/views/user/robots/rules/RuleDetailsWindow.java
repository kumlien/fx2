package hoggaster.web.vaadin.views.user.robots.rules;

import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Window;
import hoggaster.rules.Comparator;
import hoggaster.rules.indicators.Indicator;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.fields.TypedSelect;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 * Used to create/edit a rule.
 *
 * Created by svante2 on 2016-11-03.
 */
public class RuleDetailsWindow extends Window {

    public RuleDetailsWindow() {
        super("Create new Rule");
        center();

        MVerticalLayout content = new MVerticalLayout();
        content.setMargin(true);
        setContent(content);

        MTextField name = new MTextField("Name:").withNullRepresentation("the name of your rule");
        name.setNullSettingAllowed(false);
        content.addComponent(name);

        MHorizontalLayout conditions = new MHorizontalLayout().withSpacing(true);
        TypedSelect<Indicator> firstIndicator = new TypedSelect<>(Indicator.class).withSelectType(ComboBox.class).withCaption("First indicator").withNullSelectionAllowed(false);
        TypedSelect<Comparator> comparator = new TypedSelect<>(Comparator.class).withSelectType(ComboBox.class).setOptions(Comparator.values()).withCaption("Comparator").withNullSelectionAllowed(false);
        TypedSelect<Indicator> secondIndicator = new TypedSelect<>(Indicator.class).withSelectType(ComboBox.class).withCaption("Second indicator").withNullSelectionAllowed(false);
        conditions.add(firstIndicator, comparator, secondIndicator);
        content.add(conditions);

        MHorizontalLayout buttons = new MHorizontalLayout().withSpacing(true);
        Button ok = new MButton("Ok", event -> close()).withIcon(FontAwesome.CHECK_CIRCLE);
        buttons.add(ok);
        content.add(buttons);

    }

}
