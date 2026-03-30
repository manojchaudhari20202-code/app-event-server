package com.example.app;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
    features = "src/test/resources/features",
    glue = {"com.example.app.steps", "com.example.app.config"},
    plugin = {"pretty", "html:target/cucumber-reports.html"}
)
public class CucumberRunner {
}
