package com.example.app.repository.service;

import com.example.app.model.Event;
import com.example.app.repository.APIRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.test.annotation.Rollback;

@SpringBootTest
@Rollback(true) // Default behavior for the class is commit (unusual for tests)
@Transactional
public class APIServiceTest {

    @Autowired
    APIRepository apiRepository;

    private static Event globalStaticEvent;
    private static Long globalStaticEventId;

    @BeforeAll
    public static void prepareEvent(){
        globalStaticEventId = Long.parseLong("100");
        globalStaticEvent = new Event();
        globalStaticEvent.setEventId(globalStaticEventId);
        globalStaticEvent.setEventStatus(false);
        globalStaticEvent.setEventScore("213123:13123");
    }

    @BeforeEach
    public void addEventBeforeEachTest(){
        apiRepository.addEvent(globalStaticEvent);
    }

    @Test
    public void getEventTest(){
        apiRepository.getEventById(globalStaticEventId);
    }

    @Test
    public void updateEventTest(){
        apiRepository.updateEvent(globalStaticEvent);
    }


    @Test
    public void removeEventTest(){
        apiRepository.removeEvent(globalStaticEventId);
    }

    @Test
    public void upsertEventTest(){
        apiRepository.upsertEvent(globalStaticEvent);
    }

}
