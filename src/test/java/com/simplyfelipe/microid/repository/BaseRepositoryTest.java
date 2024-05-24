package com.simplyfelipe.microid.repository;

import com.simplyfelipe.microid.BaseTest;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

abstract class BaseRepositoryTest extends BaseTest {
    @Container
    protected static MySQLContainer<?> container = new MySQLContainer<>(DockerImageName.parse("mysql"));

    protected static class DataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.test.database.replace=none",
                    "spring.datasource.url=" + container.getJdbcUrl(),
                    "spring.datasource.username=" + container.getUsername(),
                    "spring.datasource.password=" + container.getPassword(),
                    "spring.datasource.driverClassName=" + container.getDriverClassName(),
                    "spring.jpa.hibernate.ddl-auto=update",
                    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect"
            );
        }
    }
}
