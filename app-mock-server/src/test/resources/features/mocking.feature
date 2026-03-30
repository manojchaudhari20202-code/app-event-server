Feature: Mock Server REST API
  As an API server consumer
  I want to call the mock server REST endpoints
  So that I receive correct event data and trigger upstream propagation

  Background:
    Given the mock server is running with seeded event data

  Scenario: Retrieve a live event by ID
    When I POST to "/api/event" with the first seeded live event id
    Then the response event has status true

  Scenario: Retrieve all live events
    When I POST to "/api/liveEvents"
    Then the response contains only events with status true

  Scenario: Retrieve an inactive event by ID
    When I POST to "/api/event" with the first seeded inactive event id
    Then the response event has status false

  Scenario: Request an unknown event returns empty response
    When I POST to "/api/event" with id "99999"
    Then the response event is empty
