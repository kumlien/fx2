package hoggaster.web.vaadin;

import com.vaadin.data.util.converter.Converter;
import hoggaster.domain.CurrencyPair;

import java.util.Locale;

/**
 * Created by svante2 on 2016-10-12.
 */
public class StringToCurrencyPairConverter implements Converter<String, CurrencyPair>{

    @Override
    public CurrencyPair convertToModel(String value, Class<? extends CurrencyPair> targetType, Locale locale) throws ConversionException {
        return CurrencyPair.valueOf(value);
    }

    @Override
    public String convertToPresentation(CurrencyPair value, Class<? extends String> targetType, Locale locale) throws ConversionException {
        return value.toString();
    }

    @Override
    public Class<CurrencyPair> getModelType() {
        return CurrencyPair.class;
    }

    @Override
    public Class<String> getPresentationType() {
        return String.class;
    }
}
