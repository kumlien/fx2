package hoggaster.web.vaadin;

import com.google.common.base.Preconditions;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.fn.tuple.Tuple3;

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
     * @param ui
     * @param values a {@link Map} with a {@link Label} as key and a {@link Tuple3} as value where the first tuple value is the new value for the label and the
     *               second tuple value is the old value for the label and the third value is a boolean indicating if new styles should be applied for positive/negative changes
     */
    public static void setAndPushDoubleLabels(UI ui, Map<Label, Tuple3<Double, Double, Boolean>> values) {


        ui.access(() -> {
            values.entrySet().forEach(entry -> {
                Double newValue = entry.getValue().getT1();
                Double oldValue = entry.getValue().getT2();
                LOG.debug("Updating from  {} to {}", oldValue, newValue);
                entry.getKey().setValue(df.format(newValue));
                if (entry.getValue().getT3()) {
                    setStyles(entry.getKey(), newValue, oldValue);
                }
            });
            ui.push();
        });

        get().getTimer().submit(l -> {
            ui.access(() -> {
                values.keySet().forEach(label -> {
                    removeStyles(label);
                });
                ui.push();
            });
        }, 1, SECONDS);

    }

    //Works for labels
    public static void setAndPushDoubleLabel(UI ui, Label label, Double newValue, Double oldValue) {
        Preconditions.checkArgument(ui != null, "UI can't be null!");
        if (newValue == oldValue) {
            return;
        }
        ui.access(() -> {
            label.setValue(newValue.toString());
            setStyles(label, newValue, oldValue);
            ui.push();
        });

        // This doesn't really work since the push is global for a ui...
        get().getTimer().submit(l -> {
            ui.access(() -> { //Needed to trigger a repaint if we get two movements in the same direction after each other (I think...)
                removeStyles(label);
                ui.push();
            });
        }, 1, SECONDS);
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
        }, 1, SECONDS);
    }

    private static void removeStyles(AbstractComponent field) {
        field.removeStyleName("pushPositive");
        field.removeStyleName("pushNegative");
    }

    private static void setStyles(AbstractComponent field, Double newValue, Double oldValue) {
        removeStyles(field);
        if (newValue > oldValue) {
            field.addStyleName("pushPositive");
        } else if (newValue < oldValue) {
            field.addStyleName("pushNegative");
        }
    }
}
