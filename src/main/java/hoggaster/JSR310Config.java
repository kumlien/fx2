package hoggaster;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Currency;


@Configuration
public class JSR310Config {

    @Bean
    public SimpleModule customSerializers(ObjectMapper jacksonObjectMapper) {
        jacksonObjectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        SimpleModule module = new SimpleModule("fx2", VERSION);
        module.addDeserializer(Currency.class, new CurrencyDeserializer());
        module.addSerializer(Currency.class, new CurrencySerializer()); // assuming serializer declares correct class to bind to
        return module;
    }

    public final static Version VERSION = VersionUtil.parseVersion(
            "0.0.1", "fx2", "custom-stuff"
    );

    public static class CurrencySerializer extends StdSerializer<Currency> {
        protected CurrencySerializer() {
            super(Currency.class);
        }

        @Override
        public void serialize(Currency currency, JsonGenerator jgen, SerializerProvider provider) throws IOException {
            jgen.writeString(currency.getCurrencyCode());
        }
    }

    public static class CurrencyDeserializer extends StdScalarDeserializer<Currency> {
        protected CurrencyDeserializer() {
            super(Currency.class);
        }

        @Override
        public Currency deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return Currency.getInstance(jp.getText());
        }
    }
}
