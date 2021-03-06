package ch.difty.scipamato.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "build")
@Getter
@Setter
public class MavenProperties {

    /**
     * The project version supplied by maven, typically {@code @project.version@}
     */
    private String version;

    /**
     * The build timestamp supplied by maven, typically {@code @timestamp@}
     */
    private String timestamp;
}
