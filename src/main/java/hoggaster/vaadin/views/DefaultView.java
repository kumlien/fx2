package hoggaster.vaadin.views;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

import javax.annotation.PostConstruct;

/**
 * Created by svante2 on 2016-02-18.
 */
@SpringView(name = "")
public class DefaultView extends VerticalLayout implements View{

    @PostConstruct
    void init() {
        addComponent(new Label("This is the default view..."));
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {

    }
}
