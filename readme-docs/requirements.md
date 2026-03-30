
## Overview
You will build a Java-based microservice (using any Java framework — for example Spring Boot) that tracks "live" sports events. For each live event, the service will periodically (every 10 seconds) call an external REST endpoint, transform the response into a message, and publish it to a message broker (e.g., Kafka). You are free to use any libraries, tools, or frameworks — and you’re encouraged to leverage AI assistants (e.g., ChatGPT, GitHub Copilot) — but you must review, validate, and document any AI-generated output.

It is expected for you to spend around 90 minutes to complete the exercise.

## Requirements
- Expose a REST endpoint to receive event status updates (`live` ↔ `not live`).
- For each event marked `live`, schedule a task to call an external REST API every 10 seconds.
    - The API returns a JSON object with the following structure:
      ```json
      {
        "eventId": "1234",
        "currentScore": "0:0"
      }
      ```
- Transform the API response into a message and publish it to a topic (e.g., using Kafka).
- Implement basic error handling and logging.
- Deliver a working prototype along with documentation of your design choices, any AI usage, and guide for running.

## Delivery
- **Executable Solution:** Your solution must be executable.
- **GitHub Link:** Provide a link to a public Git repository containing your code.
- **README.md:** Must include:
    - Setup & run instructions.
    - How to run any included tests.
    - A summary of your design decisions.
    - Documentation of any AI-assisted parts (what was generated, how you verified/improved it).

## Detailed Task Description
### A. Event Status Endpoint
a. Implement POST `/events/status` (or equivalent) that accepts a JSON payload with:
i. `eventId` (string or number)
ii. `status` (boolean or enum: “live” / “not live”)
b. Validate input and update in-memory state.

### B. Periodic REST Calls
a. For each event in “live” state, schedule a job that fires every 10 seconds.
b. Each job should call an external REST API (hardcoded or configurable endpoint).
i. The API can be mocked or implemented as a simple separate service.
c. Use the returned data to build a message payload.

### C. Message Publishing
a. Publish the payload to a RocketMQ or Kafka topic.
b. Include retry logic for transient failures.
c. Log successes and failures appropriately.

### D. Error Handling & Logging
a. Handle errors in external calls and message publishing.
b. Log key events, errors, and state changes for observability.

### E. Testing
a. Provide unit and/or integration tests covering:
i. Status updates
ii. Scheduled calls
iii. Message publication under normal and error conditions

