package com.example.app.controller;

import com.example.app.model.Event;
import com.example.app.service.MockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MockController {

    @Autowired
    MockService mockService;

    @PostMapping("/event")
    public Event event(@RequestBody String eventId) {
        return mockService.getEvent(eventId);
    }

    @PostMapping("/liveEvents")
    public List<Event> liveEvents() {
        return mockService.getLiveEvents();
    }

}
