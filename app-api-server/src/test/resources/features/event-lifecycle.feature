Feature: Event Lifecycle — CQRS Write Operations
  As an internal consumer (RocketMQ message processor)
  I want to add, update, upsert, and remove events in the API server store
  So that the event state stays consistent with the mock server

  Background:
    Given the API server is running with seeded event data

  Scenario: Add a new event then retrieve it
    Given a new event with id 100, status true, and score "NEW:SCORE"
    When the event is added to the store
    Then querying by id 100 returns the event with score "NEW:SCORE"

  Scenario: Update an existing event score
    Given event with id 1 exists in the store
    When the event score is updated to "UPDATED:SCORE"
    Then querying by id 1 returns the event with score "UPDATED:SCORE"

  Scenario: Upsert inserts a non-existing event
    Given no event with id 200 exists in the store
    When event with id 200, status true, and score "UPSERT:NEW" is upserted
    Then querying by id 200 returns the event with score "UPSERT:NEW"

  Scenario: Upsert updates an existing event
    Given event with id 2 exists in the store
    When event with id 2, status true, and score "UPSERT:UPDATE" is upserted
    Then querying by id 2 returns the event with score "UPSERT:UPDATE"

  Scenario: Remove an existing event
    Given event with id 5 exists in the store
    When event with id 5 is removed
    Then querying by id 5 returns null
