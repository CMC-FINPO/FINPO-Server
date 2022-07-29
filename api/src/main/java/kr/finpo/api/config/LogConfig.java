package kr.finpo.api.config;

import ch.qos.logback.access.tomcat.LogbackValve;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;


@Configuration
public class LogConfig {

    @Autowired
    ResourceLoader resourceLoader;

    @Bean
    public WebServerFactoryCustomizer embeddedServletContainerCustomizer() {
        return container -> {
            if (container instanceof TomcatServletWebServerFactory) {
                ((TomcatServletWebServerFactory) container).addContextCustomizers(context -> {
                    LogbackValve valve = new LogbackValve();
                    valve.setFilename(
                        resourceLoader.getResource(ResourceLoader.CLASSPATH_URL_PREFIX + "logback-access.xml")
                            .getFilename());
                    context.getPipeline().addValve(valve);
                });
            }
        };
    }
}