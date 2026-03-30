Feature: Random Event Generation (@Scheduled batch)
  As the mock server scheduler
  I want to insert 3 new live events and deactivate 1 existing event every 5 seconds
  So that the API server always has fresh, realistic event data to consume

  Background:
    Given the mock server is running with seeded event data

  Scenario: Batch inserts three new live events
    When the data generator runs one cycle
    Then 3 new live events are present in the store

  Scenario: Batch deactivates one random existing event
    When the data generator runs one cycle
    Then at least one event in the store has status false

  Scenario: Inserted events have a valid score format
    When the data generator runs one cycle
    Then all newly inserted events have a score matching "N:M" pattern

  Scenario: Data generator sends all batch events to message broker
    When the data generator runs one cycle
    Then 4 messages are sent to the event topic
