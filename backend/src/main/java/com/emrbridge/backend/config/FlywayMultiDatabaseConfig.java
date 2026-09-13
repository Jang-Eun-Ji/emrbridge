package com.emrbridge.backend.config;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import javax.sql.DataSource;

@Configuration
public class FlywayMultiDatabaseConfig {

    @Value("${spring.datasource.url}")
    private String emrUrl;

    @Value("${spring.datasource.username}")
    private String emrUsername;

    @Value("${spring.datasource.password}")
    private String emrPassword;

    @Value("${spring.datasource.driver-class-name}")
    private String emrDriverClassName;

    @Value("${spring.analytics-db.datasource.url}")
    private String analyticsUrl;

    @Value("${spring.analytics-db.datasource.username}")
    private String analyticsUsername;

    @Value("${spring.analytics-db.datasource.password}")
    private String analyticsPassword;

    @Value("${spring.analytics-db.datasource.driver-class-name}")
    private String analyticsDriverClassName;

    @Bean(name = "emrDataSource")
    @Primary
    public DataSource emrDataSource() {
        return DataSourceBuilder.create()
                .url(emrUrl)
                .username(emrUsername)
                .password(emrPassword)
                .driverClassName(emrDriverClassName)
                .build();
    }

    @Bean(name = "analyticsDataSource")
    public DataSource analyticsDataSource() {
        return DataSourceBuilder.create()
                .url(analyticsUrl)
                .username(analyticsUsername)
                .password(analyticsPassword)
                .driverClassName(analyticsDriverClassName)
                .build();
    }

    @Bean(name = "emrFlyway")
    @Primary
    public Flyway emrFlyway(@Qualifier("emrDataSource") DataSource emrDataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(emrDataSource)
                .locations("classpath:db/migration/emr")
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
        return flyway;
    }

    @Bean(name = "analyticsFlyway")
    public Flyway analyticsFlyway(@Qualifier("analyticsDataSource") DataSource analyticsDataSource) {
        Flyway flyway = Flyway.configure()
                .dataSource(analyticsDataSource)
                .locations("classpath:db/migration/analytics")
                .baselineOnMigrate(true)
                .load();
        flyway.migrate();
        return flyway;
    }

    /**
     * JPA must start only after both Flyway migrations complete.
     */
    @Bean
    static BeanFactoryPostProcessor flywayBeforeJpaPostProcessor() {
        return beanFactory -> {
            if (beanFactory.containsBeanDefinition("entityManagerFactory")) {
                beanFactory.getBeanDefinition("entityManagerFactory")
                        .setDependsOn("emrFlyway", "analyticsFlyway");
            }
            if (beanFactory.containsBeanDefinition("analyticsEntityManagerFactory")) {
                beanFactory.getBeanDefinition("analyticsEntityManagerFactory")
                        .setDependsOn("analyticsFlyway");
            }
        };
    }
}
