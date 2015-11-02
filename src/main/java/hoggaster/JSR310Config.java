package hoggaster;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.util.VersionUtil;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.Currency;


@Configuration
public class JSR310Config {


    @Bean
    public ObjectMapper jacksonObjectMapper() {
        return new ObjectMapper().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * @return ObjectMapper module for java.time types
     */
    @Bean
    public JSR310Module jsr310Module() {
        return new JSR310Module();
    }

    @Bean
    public SimpleModule customSerializers(ObjectMapper jacksonObjectMapper) {

        SimpleModule testModule = new SimpleModule("MyModule", VERSION);
        testModule.addDeserializer(Currency.class, new CurrencyDeserializer());
        testModule.addSerializer(Currency.class, new CurrencySerializer()); // assuming serializer declares correct class to bind to
        return testModule;
    }

    public final static Version VERSION = VersionUtil.parseVersion(
            "0.0.1", "hoggaster", "custom-stuff"
    );

    public static class CurrencySerializer extends StdSerializer<Currency> {

        protected CurrencySerializer() {
            super(Currency.class);
        }

        @Override
        public void serialize(Currency currency, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
            jgen.writeString(currency.getCurrencyCode());
        }
    }

    public static class CurrencyDeserializer extends StdScalarDeserializer<Currency> {

        protected CurrencyDeserializer() {
            super(Currency.class);
        }

        @Override
        public Currency deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            return Currency.getInstance(jp.getText());
        }
    }


}
