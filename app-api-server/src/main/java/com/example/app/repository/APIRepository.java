package com.example.app.repository;

import com.example.app.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class APIRepository implements RowMapper<Event> {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Value("${app.event.get.sql}")
    private String getEventSql;

    @Value("${app.event.getalllive.sql}")
    private String getAllLiveEventSql;

    @Value("${app.event.add.sql}")
    private String addEventSql;

    @Value("${app.event.update.sql}")
    private String updateEventSql;

    @Value("${app.event.remove.sql}")
    private String removeEventSql;

    public List<Event> getAllLiveEvents() {
        List<Event> liveEvents;
        try {
            liveEvents = jdbcTemplate.query(getAllLiveEventSql,this);
        } catch (Exception ex) {
            liveEvents = new ArrayList<>();
        }
        return liveEvents;
    }

    public Event getEventById(Long eventId) {
        Event event = null;
        try {
            event = jdbcTemplate.queryForObject(getEventSql,this,eventId);
        } catch(Exception ex){
            // TODO
        }
        return event;
    }

    public void upsertEvent(Event event) {
        Event existingEvent = getEventById(event.getEventId());
        if(existingEvent != null){
            updateEvent(event);
        } else {
            addEvent(event);
        }
    }

    public void addEvent(Event event) {
        jdbcTemplate.update(addEventSql, event.getEventId(), event.isEventStatus(), event.getEventScore());
    }

    public void updateEvent(Event event) {
        jdbcTemplate.update(updateEventSql, event.isEventStatus(), event.getEventScore(),event.getEventId());
    }

    public void removeEvent(Long eventId) {
        jdbcTemplate.update(removeEventSql,eventId);
    }

    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        Event event = new Event();
        event.setEventId(rs.getLong("EVENT_ID"));
        event.setEventStatus( rs.getBoolean("EVENT_STATUS"));
        event.setEventScore(rs.getString("EVENT_SCORE"));
        return event;
    }

    public Long getRandonEventId() {
        return jdbcTemplate.queryForObject("SELECT EVENT_ID FROM mock_event_details ORDER BY RANDOM() OFFSET 0 ROWS FETCH NEXT 1 ROW ONLY", Long.class);
    }

    public Event getRandonEvent() {
        return getEventById(getRandonEventId());
    }

}
