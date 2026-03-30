package com.example.app.controller;

import com.example.app.model.Event;
import com.example.app.model.EventStatusUpdateRequest;
import com.example.app.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/event")
public class EventController {

    @Autowired
    private EventService service;

    @PatchMapping("/{id}/status")
    public ResponseEntity<Event> updateEventStatus(
            @PathVariable Long id,
            @RequestBody EventStatusUpdateRequest request) {
        Event updated = new Event();
        return ResponseEntity.ok(updated);
    }

}
