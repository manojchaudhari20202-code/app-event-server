package com.example.app.service;

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

/**
*    @author Manoj
**/

public class APIServiceTest {

    @Autowired
    APIRepository apiRepository;

    private static Event globalStaticEvent;
    private static Long globalStaticEventId;

    @BeforeEach
    public void addEventBeforeEachTest(){
        globalStaticEvent = apiRepository.getRandonEvent();
        globalStaticEventId = globalStaticEvent.getEventId();
    }

    @Test
    public void getEventTest(){
        apiRepository.getEventById(globalStaticEventId);
    }

    @Test
    public void updateEventTest(){
        globalStaticEvent.setEventStatus(false);
        globalStaticEvent.setEventScore("213123:13123");
        apiRepository.updateEvent(globalStaticEvent);
    }


    @Test
    public void removeEventTest(){
        apiRepository.removeEvent(globalStaticEventId);
    }

    @Test
    public void upsertEventTest(){
        globalStaticEvent.setEventStatus(false);
        globalStaticEvent.setEventScore("213123:13123");
        apiRepository.upsertEvent(globalStaticEvent);
    }

}
