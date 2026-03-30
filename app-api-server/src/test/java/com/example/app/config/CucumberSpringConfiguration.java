package com.example.app.config;

import com.example.app.util.APIIntegrationClient;
import io.cucumber.spring.CucumberContextConfiguration;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Bridges Cucumber with the Spring application context.
 * All BDD step definitions run inside a real (test-profile) Spring Boot context
 * so they can @Autowire any Spring bean.
 */
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class CucumberSpringConfiguration {

    @MockitoBean
    public RocketMQTemplate rocketMQTemplate;

    @MockitoBean
    public APIIntegrationClient apiIntegrationClient;
}
