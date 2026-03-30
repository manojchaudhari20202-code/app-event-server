package com.example.app.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GreetingSteps {

    private String greetingPageStatus;
    private String userMessage;
    private String systemResponse;

    @Given("the user is on the greeting page")
    public void the_user_is_on_the_greeting_page() {
        greetingPageStatus = "on";
    }

    @When("the user says {string}")
    public void the_user_says(String message) {
        userMessage = message;
        if ("on".equals(greetingPageStatus)) {
            if ("Hello".equals(userMessage)) {
                systemResponse = "Hello there!";
            } else {
                systemResponse = "I don't understand.";
            }
        } else {
            systemResponse = "User is not on greeting page.";
        }
    }

    @Then("the system should respond with {string}")
    public void the_system_should_respond_with(String expectedResponse) {
        assertEquals(expectedResponse, systemResponse);
    }
}
