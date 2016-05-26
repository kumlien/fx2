package hoggaster;

import hoggaster.oanda.OandaProperties;
import hoggaster.oanda.OandaResourcesProperties;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(value = {OandaProperties.class, OandaResourcesProperties.class})
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    EmbeddedServletContainerCustomizer servletContainerCustomizer() {
        return servletContainer -> ((TomcatEmbeddedServletContainerFactory)servletContainer)
                .addConnectorCustomizers(connector -> {
                    AbstractHttp11Protocol httpProtocol = (AbstractHttp11Protocol) connector.getProtocolHandler();
                    httpProtocol.setCompression("on");
                    httpProtocol.setCompressionMinSize(256);
                    String mimeTypes = httpProtocol.getCompressableMimeTypes();
                    String mimeTypesWithJson = mimeTypes + ","
                            + MediaType.APPLICATION_JSON_VALUE
                            + ",application/javascript";
                    httpProtocol.setCompressableMimeType(mimeTypesWithJson);
                });
    }

}
