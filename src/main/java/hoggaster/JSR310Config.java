package hoggaster;


@Configuration
public class JSR310Config {
    


    /**
     * @return override for Spring Web's default JAXB2 converter using safe deserialization (not relying on XmlRootElement annotation on target class)
     */
    @Bean
    @Primary
    public Jaxb2RootElementHttpMessageConverter jaxb2RootElementHttpMessageConverter() {
        return new SafeJaxb2RootElementHttpMessageConverter();
    }

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
