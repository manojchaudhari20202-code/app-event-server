package com.example.app;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class LiveEventService {

    private final Logger logger = LoggerFactory.getLogger(LiveEventService.class);

    //-- Eager Initialisation
    private final Map<String, LiveEvent> liveEventsData = new ConcurrentHashMap<>(1_00_000);

    //--
    public void updateCache(String eventId, boolean status) {
        if (!status) {
            liveEventsData.remove(eventId);
        } else {
            LiveEvent liveEvent = new LiveEvent();
            liveEvent.setEventId(eventId);
            liveEvent.setLive(true);
            liveEventsData.put(eventId,liveEvent);
            System.out.println(liveEventsData);
            System.out.println(liveEventsData.get(eventId));
        }
    }

    public LiveEvent getFromCache(String eventId) {
        return liveEventsData.get(eventId);
    }

    public List<LiveEvent> getAllEventsFromLiveCache() {
        return new ArrayList<>(liveEventsData.values());
    }

    public List<String> getAllEventIdsFromLiveCache() {
        return new ArrayList<>(liveEventsData.keySet());
    }

}