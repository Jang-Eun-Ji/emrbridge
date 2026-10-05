package com.emrbridge.backend.feature.analytics.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.emrbridge.backend.feature.analytics.repository",
        entityManagerFactoryRef = "analyticsEntityManagerFactory",
        transactionManagerRef = "analyticsTransactionManager"
)
public class AnalyticsJpaConfig {

    @Bean(name = "analyticsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean analyticsEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("analyticsDataSource") DataSource analyticsDataSource) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", "create-drop"); // DDL AUTO Setting for development; change to 'validate' or 'update' in production
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");

        return builder
                .dataSource(analyticsDataSource)
                .packages("com.emrbridge.backend.feature.analytics.domain")
                .persistenceUnit("analytics")
                .properties(properties)
                .build();
    }

    @Bean(name = "analyticsTransactionManager")
    public PlatformTransactionManager analyticsTransactionManager(
            @Qualifier("analyticsEntityManagerFactory") EntityManagerFactory analyticsEntityManagerFactory) {
        return new JpaTransactionManager(analyticsEntityManagerFactory);
    }
}
