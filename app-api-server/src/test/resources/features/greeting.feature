Feature: Greeting
  Basic sanity check that the BDD harness is wired up correctly.

  Scenario: User says hello
    Given the user is on the greeting page
    When the user says "Hello"
    Then the system should respond with "Hello there!"

  Scenario: User says an unrecognised message
    Given the user is on the greeting page
    When the user says "Goodbye"
    Then the system should respond with "I don't understand."
