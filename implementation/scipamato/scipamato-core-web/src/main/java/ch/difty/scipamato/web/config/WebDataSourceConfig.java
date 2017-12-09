package ch.difty.scipamato.web.config;

import javax.sql.DataSource;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebDataSourceConfig {

    @Value("${spring.jooq.sql-dialect}")
    private String dialect;

    /**
     * Scipamato-Core as primary datasource
     */
    @Bean
    @ConfigurationProperties(prefix = "sync.source.datasource")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    /**
     * @return {@link org.jooq.Configuration} for scipamato-core
     */
    @Bean
    public org.jooq.Configuration configuration() {
        return considerSettingDialect(new DefaultConfiguration().derive(dataSource()));
    }

    /**
     * Set dialect into configuration - if possible
     */
    private org.jooq.Configuration considerSettingDialect(org.jooq.Configuration configuration) {
        if (dialect != null)
            return configuration.derive(SQLDialect.valueOf(dialect));
        else
            return configuration;
    }

    /**
     * @return {@link DSLContext} for scipamato-core
     */
    @Bean
    public DSLContext dslContext() {
        return DSL.using(configuration());
    }

}