package hoggaster.web.vaadin;

import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import reactor.Environment;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by svante.kumlien on 03.03.16.
 */
public class GuiUtils {

    public static void setAndPushDoubleLabel(UI ui, Label label, Double newValue, Double oldValue) {
        ui.access(() -> {
            label.setValue(newValue.toString());
            label.removeStyleName("pushPositive");
            label.removeStyleName("pushNegative");
            if (newValue > oldValue) {
                label.addStyleName("pushPositive");
            } else if (oldValue < newValue) {
                label.addStyleName("pushNegative");
            }
            ui.push(); //If price same for second time in a row we dont need to push
        });
        Environment.get().getTimer().submit(l -> {
            ui.access(() -> { //Needed to trigger a repaint if we get two movements in the same direction after each other (I think...)
                label.removeStyleName("pushPositive");
                label.removeStyleName("pushNegative");
                ui.push();
            });
        },1, SECONDS);
    }
}
