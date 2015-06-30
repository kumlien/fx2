package hoggaster;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;


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


}
