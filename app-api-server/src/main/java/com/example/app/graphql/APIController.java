package com.example.app.graphql;

import com.example.app.model.Event;
import com.example.app.service.APIService;
import com.example.app.util.APIIntegrationClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class APIController {

    private final Logger logger = LoggerFactory.getLogger(APIController.class);

    @Autowired
    APIService apiService;

    @Autowired
    APIIntegrationClient apiIntegrationClient;

    @QueryMapping
    public Event eventById(@Argument String eventId) {
        apiIntegrationClient.getRemoteEventStatus(eventId);
        return apiService.getCachedEventById(eventId);
    }

    @QueryMapping
    public List<Event> liveEvents() {
        return apiService.getAllLiveEvents();
    }

}
