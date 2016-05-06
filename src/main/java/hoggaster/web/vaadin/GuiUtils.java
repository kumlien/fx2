package hoggaster.web.vaadin;

import com.google.common.base.Preconditions;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.fn.tuple.Tuple2;

import java.text.DecimalFormat;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static reactor.Environment.get;

/**
 * Created by svante.kumlien on 03.03.16.
 */
public class GuiUtils {

    public static final Logger LOG = LoggerFactory.getLogger(GuiUtils.class);

    public static final DecimalFormat df = new DecimalFormat("0.0000");

    /**
     *
     * @param ui
     * @param values
     *            a {@link Map} with a {@link Label} as key and a {@link Tuple2} as value where the first tuple value is the new value for the label and the
     *            second tuple value is the old value for the label.
     */
    public static void setAndPushDoubleLabels(UI ui, Map<Label, Tuple2<Double, Double>> values) {
        values.entrySet().forEach(entry -> {
            Double newValue = entry.getValue().getT1();
            Double oldValue = entry.getValue().getT2();
                LOG.debug("Updating from  {} to {}", oldValue, newValue);
                entry.getKey().setValue(df.format(newValue));
                entry.getKey().removeStyleName("pushPositive");
                entry.getKey().removeStyleName("pushNegative");
                if (newValue >= oldValue) {
                    entry.getKey().addStyleName("pushPositive");
                } else {
                    entry.getKey().addStyleName("pushNegative");
                }
        });

        ui.access(ui::push);

        get().getTimer().submit(l -> {
            values.keySet().forEach(label -> {
                label.removeStyleName("pushPositive");
                label.removeStyleName("pushNegative");
            });
            ui.access(ui::push);
        } , 2, SECONDS);

    }

    //Works for labels
    public static void setAndPushDoubleLabel(UI ui, Label label, Double newValue, Double oldValue) {
        Preconditions.checkArgument(ui != null, "UI can't be null!");
        if (newValue == oldValue) {
            return;
        }
        label.setValue(newValue.toString());
        label.removeStyleName("pushPositive");
        label.removeStyleName("pushNegative");
        if (newValue > oldValue) {
            label.addStyleName("pushPositive");
        } else {
            label.addStyleName("pushNegative");
        }
        ui.access(() -> {
            ui.push();
        });

        get().getTimer().submit(l -> {
            label.removeStyleName("pushPositive");
            label.removeStyleName("pushNegative");
            ui.access(() -> { //Needed to trigger a repaint if we get two movements in the same direction after each other (I think...)
                ui.push();
            });
        } , 2, SECONDS);
    }

    //Works for text fields
    public static void setAndPushDoubleField(UI ui, AbstractTextField field, Double newValue, Double oldValue) {
        Preconditions.checkArgument(ui != null, "UI can't be null!");
        if (newValue == oldValue)
            return;
        ui.access(() -> {
            field.setValue(newValue.toString());
            setStyles(field, newValue, oldValue);
            ui.push(); //If price same for second time in a row we don't need to push
        });
        get().getTimer().submit(l -> {
            ui.access(() -> { //Needed to trigger a repaint if we get two movements in the same direction after each other (I think...)
                removeStyles(field);
                ui.push();
            });
        } , 2, SECONDS);
    }

    private static void removeStyles(AbstractTextField field) {
        field.removeStyleName("pushPositive");
        field.removeStyleName("pushNegative");
    }

    private static void setStyles(AbstractTextField field, Double newValue, Double oldValue) {
        removeStyles(field);
        if (newValue > oldValue) {
            field.addStyleName("pushPositive");
        } else if (newValue < oldValue) {
            field.addStyleName("pushNegative");
        }
    }
}
