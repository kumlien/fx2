package hoggaster.web.vaadin;

import com.vaadin.data.util.converter.Converter;
import com.vaadin.data.util.converter.DefaultConverterFactory;
import hoggaster.domain.CurrencyPair;

/**
 * Created by svante2 on 2016-10-12.
 */
public class Fx2ConverterFactory extends DefaultConverterFactory {

    @Override
    protected <PRESENTATION, MODEL> Converter<PRESENTATION, MODEL> findConverter(Class<PRESENTATION> presentationType, Class<MODEL> modelType) {
        // Handle String <-> Double
        if (presentationType == String.class && modelType == CurrencyPair.class) {
            return (Converter<PRESENTATION, MODEL>) new StringToCurrencyPairConverter();
        }
        // Let default factory handle the rest
        return super.findConverter(presentationType, modelType);
    }
}
