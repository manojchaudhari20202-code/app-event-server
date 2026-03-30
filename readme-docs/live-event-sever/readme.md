## Build and Deploy Instructions [Sequence]

### Checkout main branch
- git clone -b main git@github.com:manojchaudhari20202-code/app-event-server.git
- OR Donwload ::: https://github.com/manojchaudhari20202-code/app-event-server/archive/refs/heads/main.zip

### Test
mvn clean test surefire-report:report-only -Dmaven.test.failure.ignore=true

### Build
mvn clean compile -DskipTests

### Start Mock Server
- cd mock-server
- mvn clean compile exec:java -Dexec.mainClass="com.example.app.MockServer"
- Swagger URL ::: http://localhost:9090/swagger-ui/index.html
- Health URL ::: http://localhost:9090/actuator/health

### Start LIVE Server
- cd live-server
- mvn clean compile exec:java -Dexec.mainClass="com.example.app.LiveEventServer"
- Swagger URL ::: http://localhost:8080/swagger-ui/index.html
- Health URL ::: http://localhost:8080/actuator/health

## Design and Implementation

### Modules

#### 1. app-infra-server
* Mock REST APIs
* Cleans message store
* Starts RocketMQ broker
 
#### 2. live-server
* Cache management (Hashmap)
* MQ publisher
* CDC consumer integration





## 
### A. Event Status Endpoint
a. Implement POST `/events/status` (or equivalent) that accepts a JSON payload with:
i. `eventId` (string or number)
ii. `status` (boolean or enum: “live” / “not live”)
b. Validate input and update in-memory state.

http://localhost:888/events/status
{
"eventId": "string",
"live": true,
"currentScore": "string"
}


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













































Below is a **Staff+ level Spring Boot Test Pyramid** tailored to your uploaded codebase (Controller + Service + Scheduler + RocketMQ Listener). I’ve mapped **real test cases directly to your classes** like:

*  `LiveEventController`
*  `LiveEventService`
*  `LiveEventListener`
*  `LiveEventScheduler`

---

# 🧪 TEST PYRAMID (Spring Boot – Your Project)

```
            ▲
           / \
          /E2E\        (Few)
         /-----\
        /  INT  \      (Some)
       /---------\
      /   UNIT    \    (Many)
     /-------------\
```

---

# 🔹 1. UNIT TESTS (Majority – Fast, Isolated)

## ✅ Target: `LiveEventService`

### Test Class

```java
@ExtendWith(MockitoExtension.class)
class LiveEventServiceTest {

    private LiveEventService service;

    @BeforeEach
    void setup() {
        service = new LiveEventService();
    }
}
```

---

### 🧩 Test Cases

#### 1. Add Event to Cache

```java
@Test
void shouldAddEventWhenStatusTrue() {
    service.updateCache("101", true);

    LiveEvent event = service.getFromCache("101");

    assertNotNull(event);
    assertTrue(event.getLive());
}
```

---

#### 2. Remove Event When Status False

```java
@Test
void shouldRemoveEventWhenStatusFalse() {
    service.updateCache("101", true);
    service.updateCache("101", false);

    assertNull(service.getFromCache("101"));
}
```

---

#### 3. Get All Live Events

```java
@Test
void shouldReturnAllLiveEvents() {
    service.updateCache("101", true);
    service.updateCache("102", true);

    List<LiveEvent> events = service.getAllEventsFromLiveCache();

    assertEquals(2, events.size());
}
```

---

#### 4. Empty Cache Case

```java
@Test
void shouldReturnEmptyListWhenNoEvents() {
    assertTrue(service.getAllEventsFromLiveCache().isEmpty());
}
```

---

# 🔹 2. CONTROLLER UNIT TEST (Web Layer Slice)

## ✅ Target: `LiveEventController`

### Test Class

```java
@WebMvcTest(LiveEventController.class)
class LiveEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LiveEventService service;
}
```

---

### 🧩 Test Cases

#### 1. Valid Event Status Update

```java
@Test
void shouldReturnSuccessForValidEvent() throws Exception {
    String json = """
        {"eventId":"101","live":true}
    """;

    mockMvc.perform(post("/events/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(status().isOk())
        .andExpect(content().string("SUCCESS"));

    verify(service).updateCache("101", true);
}
```

---

#### 2. Invalid Event → FAILED

```java
@Test
void shouldReturnFailedForInvalidEvent() throws Exception {
    String json = """
        {"eventId":"abc","live":true}
    """;

    mockMvc.perform(post("/events/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
        .andExpect(content().string("FAILED"));
}
```

---

#### 3. Get All Live Events

```java
@Test
void shouldReturnAllLiveEvents() throws Exception {
    when(service.getAllEventsFromLiveCache()).thenReturn(List.of(new LiveEvent()));

    mockMvc.perform(post("/events/live"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
}
```

---

#### 4. Get Event From Cache

```java
@Test
void shouldReturnEventFromCache() throws Exception {
    LiveEvent event = new LiveEvent();
    event.setEventId("101");

    when(service.getFromCache("101")).thenReturn(event);

    mockMvc.perform(post("/events/get")
            .content("101"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].eventId").value("101"));
}
```

---

# 🔹 3. INTEGRATION TESTS (Spring Context + Real Beans)

## ✅ Target: Service + Controller + Cache

```java
@SpringBootTest
@AutoConfigureMockMvc
class LiveEventIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LiveEventService service;
}
```

---

### 🧩 Test Cases

#### 1. End-to-End Flow (Controller → Service → Cache)

```java
@Test
void shouldStoreAndFetchEvent() throws Exception {

    mockMvc.perform(post("/events/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"eventId":"200","live":true}"""))
        .andExpect(status().isOk());

    mockMvc.perform(post("/events/get")
            .content("200"))
        .andExpect(jsonPath("$[0].eventId").value("200"));
}
```

---

#### 2. Cache Consistency

```java
@Test
void shouldReflectCacheStateAcrossCalls() {
    service.updateCache("300", true);

    assertEquals(1, service.getAllEventsFromLiveCache().size());
}
```

---

# 🔹 4. MESSAGE / ASYNC TESTS (RocketMQ Listener)

## ✅ Target: `LiveEventListener`

```java
@ExtendWith(MockitoExtension.class)
class LiveEventListenerTest {

    @InjectMocks
    private LiveEventListener listener;

    @Mock
    private LiveEventService service;
}
```

---

### 🧩 Test Cases

#### 1. Update Score on Message

```java
@Test
void shouldUpdateScoreOnMessage() {
    LiveEvent existing = new LiveEvent();
    existing.setEventId("101");

    when(service.getFromCache("101")).thenReturn(existing);

    LiveEvent incoming = new LiveEvent();
    incoming.setEventId("101");
    incoming.setCurrentScore("100/2");

    listener.onMessage(incoming);

    assertEquals("100/2", existing.getCurrentScore());
}
```

---

#### 2. Null Event Handling (Edge Case)

```java
@Test
void shouldHandleNullEventGracefully() {
    when(service.getFromCache("101")).thenReturn(null);

    LiveEvent incoming = new LiveEvent();
    incoming.setEventId("101");

    assertDoesNotThrow(() -> listener.onMessage(incoming));
}
```

⚠️ (Your current code may throw NPE → improvement opportunity)

---

# 🔹 5. SCHEDULER TESTS

## ✅ Target: `LiveEventScheduler`

---

### 🧩 Test Strategy

* Mock:

    * `LiveEventService`
    * `RocketMQTemplate`
    * REST call

---

### Example

```java
@ExtendWith(MockitoExtension.class)
class LiveEventSchedulerTest {

    @Mock
    private LiveEventService service;

    @Mock
    private RocketMQTemplate rocketMQTemplate;

    @InjectMocks
    private LiveEventScheduler scheduler;
}
```

---

#### Test Case: Trigger Scheduled Flow

```java
@Test
void shouldSendEventsToRocketMQ() throws Exception {
    when(service.getAllEventIdsFromLiveCache())
        .thenReturn(List.of("101"));

    scheduler.prepareLiveMockData();

    verify(service).getAllEventIdsFromLiveCache();
}
```

---

# 🔹 6. E2E TESTS (Few but Critical)

## Tools:

* RestAssured
* Testcontainers (RocketMQ)
* WireMock (mock server)

---

### Example

```java
@Test
void fullFlow_shouldWork() {
    given()
        .contentType("application/json")
        .body("""{"eventId":"500","live":true}""")
    .when()
        .post("/events/status")
    .then()
        .statusCode(200);

    given()
        .body("500")
    .when()
        .post("/events/get")
    .then()
        .body("[0].eventId", equalTo("500"));
}
```

---

# 🔥 STAFF+ LEVEL IMPROVEMENTS (IMPORTANT)

Your current system has **testability gaps**:

### 🚨 Issues Found

* ❌ `LiveEventListener` → possible NPE
* ❌ Scheduler tightly coupled with REST
* ❌ No interface abstraction
* ❌ No validation layer

---

### ✅ Improvements

1. Add validation layer
2. Add repository abstraction
3. Use `@Transactional` where needed
4. Add contract tests (consumer-driven)
5. Add performance tests (important for 100 threads MQ)

---

# 📦 FINAL TEST STRUCTURE

```
src/test/java
 ├── unit
 │    ├── service/
 │    ├── listener/
 │
 ├── web
 │    ├── controller/
 │
 ├── integration
 │
 ├── scheduler
 │
 ├── e2e
```

---

# 🚀 If You Want Next Level

I can generate for you:

✅ Full **1MB+ production-grade test project (ZIP)**
✅ With:

* Testcontainers (RocketMQ)
* WireMock
* Cucumber BDD
* Performance tests (JMH/Gatling)
* CI pipeline

Just say: **"generate elite test project"**
