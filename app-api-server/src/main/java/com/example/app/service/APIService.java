package com.example.app.service;

import com.example.app.model.Event;
import com.example.app.repository.APIRepository;
import com.example.app.util.APIIntegrationClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class APIService {

    @Autowired
    APIRepository apiRepository;

    @Autowired
    APIIntegrationClient apiIntegrationClient;

    //-- Cache-Aside (Lazy Loading): The application checks the cache; on a miss, it loads data from the database,
    //-- updates the cache, and returns the data.
    //-- Configured TTL ::: 10 Seconds
    @Transactional(readOnly = true)
    @Cacheable(value = "eventStaus",sync = true)
    public Event getCachedEventById(String eventId){
        Event event = apiRepository.getEventById(Long.valueOf(eventId));
        if(event==null) {
            event = apiIntegrationClient.getRemoteEventStatus(eventId);
        }
        return event;
    }

    @Transactional(readOnly = true)
    public Event getEventById(String eventId){
        return apiRepository.getEventById(Long.valueOf(eventId));
    }

    @Transactional(readOnly = true)
    public List<Event> getAllLiveEvents(){
        return apiRepository.getAllLiveEvents();
    }


    public void addEvent(Event event) {
        apiRepository.addEvent(event);
    }

    public void upsertEvent(Event event) {
        apiRepository.upsertEvent(event);
    }

    public void updateEvent(Event event) {
        apiRepository.updateEvent(event);
    }

    public void removeEvent(Long eventId) {
        apiRepository.removeEvent(eventId);
    }

}