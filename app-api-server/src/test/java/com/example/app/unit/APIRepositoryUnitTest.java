package com.example.app.unit;

import com.example.app.model.Event;
import com.example.app.repository.APIRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.ResultSet;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Test Layer — APIRepository
 * Mocks JdbcTemplate to verify correct SQL delegation and RowMapper behaviour.
 */
@ExtendWith(MockitoExtension.class)
class APIRepositoryUnitTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private APIRepository apiRepository;

    private static final String GET_SQL        = "SELECT EVENT_ID,EVENT_STATUS,EVENT_SCORE FROM API_EVENT_DETAILS WHERE EVENT_ID = ?";
    private static final String GET_ALL_SQL    = "SELECT EVENT_ID,EVENT_STATUS,EVENT_SCORE FROM API_EVENT_DETAILS WHERE EVENT_STATUS = TRUE";
    private static final String ADD_SQL        = "INSERT INTO API_EVENT_DETAILS (EVENT_ID,EVENT_STATUS,EVENT_SCORE) VALUES (?,?,?)";
    private static final String UPDATE_SQL     = "UPDATE API_EVENT_DETAILS SET EVENT_STATUS = ?, EVENT_SCORE=? WHERE EVENT_ID = ?";
    private static final String REMOVE_SQL     = "DELETE FROM API_EVENT_DETAILS WHERE EVENT_ID = ?";

    @BeforeEach
    void injectSqlValues() {
        ReflectionTestUtils.setField(apiRepository, "getEventSql",      GET_SQL);
        ReflectionTestUtils.setField(apiRepository, "getAllLiveEventSql", GET_ALL_SQL);
        ReflectionTestUtils.setField(apiRepository, "addEventSql",      ADD_SQL);
        ReflectionTestUtils.setField(apiRepository, "updateEventSql",   UPDATE_SQL);
        ReflectionTestUtils.setField(apiRepository, "removeEventSql",   REMOVE_SQL);
    }

    // ── getAllLiveEvents ──────────────────────────────────────────────────────

    @Test
    void getAllLiveEvents_returnsEventsFromJdbc() {
        List<Event> expected = Arrays.asList(new Event(1L, true, "100:200"));
        when(jdbcTemplate.query(eq(GET_ALL_SQL), eq(apiRepository))).thenReturn(expected);

        List<Event> result = apiRepository.getAllLiveEvents();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getEventId());
    }

    @Test
    void getAllLiveEvents_onException_returnsEmptyList() {
        when(jdbcTemplate.query(eq(GET_ALL_SQL), eq(apiRepository))).thenThrow(new RuntimeException("DB down"));

        List<Event> result = apiRepository.getAllLiveEvents();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── getEventById ─────────────────────────────────────────────────────────

    @Test
    void getEventById_returnsEventFromJdbc() {
        Event expected = new Event(1L, true, "100:200");
        when(jdbcTemplate.queryForObject(eq(GET_SQL), eq(apiRepository), eq(1L))).thenReturn(expected);

        Event result = apiRepository.getEventById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getEventId());
    }

    @Test
    void getEventById_onException_returnsNull() {
        when(jdbcTemplate.queryForObject(eq(GET_SQL), eq(apiRepository), eq(99L)))
                .thenThrow(new RuntimeException("not found"));

        Event result = apiRepository.getEventById(99L);

        assertNull(result);
    }

    // ── addEvent ─────────────────────────────────────────────────────────────

    @Test
    void addEvent_callsJdbcUpdateWithCorrectArgs() {
        Event event = new Event(10L, true, "10:20");

        apiRepository.addEvent(event);

        verify(jdbcTemplate).update(ADD_SQL, 10L, true, "10:20");
    }

    // ── updateEvent ──────────────────────────────────────────────────────────

    @Test
    void updateEvent_callsJdbcUpdateWithCorrectArgs() {
        Event event = new Event(1L, false, "999:999");

        apiRepository.updateEvent(event);

        verify(jdbcTemplate).update(UPDATE_SQL, false, "999:999", 1L);
    }

    // ── removeEvent ──────────────────────────────────────────────────────────

    @Test
    void removeEvent_callsJdbcUpdateWithEventId() {
        apiRepository.removeEvent(1L);

        verify(jdbcTemplate).update(REMOVE_SQL, 1L);
    }

    // ── upsertEvent ──────────────────────────────────────────────────────────

    @Test
    void upsertEvent_existingEvent_callsUpdate() {
        Event event = new Event(1L, false, "new:score");
        when(jdbcTemplate.queryForObject(eq(GET_SQL), eq(apiRepository), eq(1L))).thenReturn(event);

        apiRepository.upsertEvent(event);

        verify(jdbcTemplate).update(UPDATE_SQL, false, "new:score", 1L);
        verify(jdbcTemplate, never()).update(eq(ADD_SQL), any(), any(), any());
    }

    @Test
    void upsertEvent_newEvent_callsAdd() {
        Event event = new Event(99L, true, "new:score");
        when(jdbcTemplate.queryForObject(eq(GET_SQL), eq(apiRepository), eq(99L)))
                .thenThrow(new RuntimeException("not found"));

        apiRepository.upsertEvent(event);

        verify(jdbcTemplate).update(ADD_SQL, 99L, true, "new:score");
        verify(jdbcTemplate, never()).update(eq(UPDATE_SQL), any(), any(), any());
    }

    // ── mapRow ───────────────────────────────────────────────────────────────

    @Test
    void mapRow_mapsResultSetCorrectly() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("EVENT_ID")).thenReturn(42L);
        when(rs.getBoolean("EVENT_STATUS")).thenReturn(true);
        when(rs.getString("EVENT_SCORE")).thenReturn("42:42");

        Event event = apiRepository.mapRow(rs, 0);

        assertEquals(42L, event.getEventId());
        assertTrue(event.isEventStatus());
        assertEquals("42:42", event.getEventScore());
    }
}
