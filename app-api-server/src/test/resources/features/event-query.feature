Feature: Event Query via GraphQL API
  As a consumer of the API server
  I want to query events by ID or retrieve all live events
  So that I get accurate, up-to-date event data

  Background:
    Given the API server is running with seeded event data

  Scenario: Query a known event by ID
    When I query the GraphQL endpoint for event with id "1"
    Then the response event id is 1
    And the response event status is true
    And the response event score is "100:200"

  Scenario: Query an unknown event by ID returns null
    When I query the GraphQL endpoint for event with id "9999"
    Then the response event is null

  Scenario: Query all live events returns only active events
    When I query the GraphQL endpoint for all live events
    Then all returned events have status true

  Scenario Outline: Query multiple known events by ID
    When I query the GraphQL endpoint for event with id "<id>"
    Then the response event score is "<score>"

    Examples:
      | id | score   |
      | 1  | 100:200 |
      | 3  | 500:600 |
      | 4  | 700:800 |
