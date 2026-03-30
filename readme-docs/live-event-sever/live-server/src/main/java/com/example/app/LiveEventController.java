package com.example.app;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("")
public class LiveEventController {

    private final Logger logger = LoggerFactory.getLogger(LiveEventController.class);

    @Autowired
    LiveEventService liveEventService;

    @PostMapping("/events/status")
    public String event(@RequestBody LiveEvent event) {
        if (event.getLive() != null && event.getEventId() != null && NumberUtils.isParsable(event.getEventId())) {
            logger.info("NEW EVENT REQUEST ::: {}", event.getEventId());
            liveEventService.updateCache(event.getEventId(), event.getLive());
            return "SUCCESS";
        }
        return "FAILED";
    }

    @PostMapping("/events/live")
    public List<LiveEvent> events() {
        return liveEventService.getAllEventsFromLiveCache();
    }

    @PostMapping("/events/get")
    public List<LiveEvent> eventFromCache(@RequestBody String eventId) {
        logger.info("GET EVENT REQUEST ::: {}", eventId);
        return Stream.of(liveEventService.getFromCache(eventId)).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
