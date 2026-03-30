package com.example.app.unit;

import com.example.app.model.Event;
import com.example.app.repository.MockRepository;
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
 * Unit Test Layer — MockRepository
 * Mocks JdbcTemplate to verify SQL delegation and RowMapper correctness.
 */
@ExtendWith(MockitoExtension.class)
class MockRepositoryUnitTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private MockRepository mockRepository;

    private static final String GET_SQL     = "SELECT EVENT_ID,EVENT_STATUS,EVENT_SCORE FROM MOCK_EVENT_DETAILS WHERE EVENT_ID = ?";
    private static final String GET_ALL_SQL = "SELECT EVENT_ID,EVENT_STATUS,EVENT_SCORE FROM MOCK_EVENT_DETAILS WHERE EVENT_STATUS = TRUE";
    private static final String UPDATE_SQL  = "UPDATE MOCK_EVENT_DETAILS SET EVENT_STATUS = ?, EVENT_SCORE=? WHERE EVENT_ID = ?";
    private static final String REMOVE_SQL  = "DELETE FROM MOCK_EVENT_DETAILS WHERE EVENT_ID = ?";

    @BeforeEach
    void injectSqlValues() {
        ReflectionTestUtils.setField(mockRepository, "getEventSql",       GET_SQL);
        ReflectionTestUtils.setField(mockRepository, "getAllLiveEventSql", GET_ALL_SQL);
        ReflectionTestUtils.setField(mockRepository, "updateEventSql",    UPDATE_SQL);
        ReflectionTestUtils.setField(mockRepository, "removeEventSql",    REMOVE_SQL);
    }

    // ── getAllLiveEvents ──────────────────────────────────────────────────────

    @Test
    void getAllLiveEvents_returnsEventsFromJdbc() {
        List<Event> expected = Arrays.asList(new Event(1L, true, "100:200"));
        when(jdbcTemplate.query(eq(GET_ALL_SQL), eq(mockRepository))).thenReturn(expected);

        List<Event> result = mockRepository.getAllLiveEvents();

        assertEquals(1, result.size());
        assertTrue(result.get(0).isEventStatus());
    }

    @Test
    void getAllLiveEvents_onException_returnsEmptyList() {
        when(jdbcTemplate.query(eq(GET_ALL_SQL), eq(mockRepository))).thenThrow(new RuntimeException("DB error"));

        List<Event> result = mockRepository.getAllLiveEvents();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── getEventById ─────────────────────────────────────────────────────────

    @Test
    void getEventById_returnsEventFromJdbc() {
        Event expected = new Event(1L, true, "100:200");
        when(jdbcTemplate.queryForObject(eq(GET_SQL), eq(mockRepository), eq(1L))).thenReturn(expected);

        Event result = mockRepository.getEventById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getEventId());
    }

    @Test
    void getEventById_onException_returnsNull() {
        when(jdbcTemplate.queryForObject(eq(GET_SQL), eq(mockRepository), eq(99L)))
                .thenThrow(new RuntimeException("not found"));

        Event result = mockRepository.getEventById(99L);

        assertNull(result);
    }

    // ── updateEvent ──────────────────────────────────────────────────────────

    @Test
    void updateEvent_callsJdbcWithCorrectArgs() {
        Event event = new Event(1L, false, "999:999");

        mockRepository.updateEvent(event);

        verify(jdbcTemplate).update(UPDATE_SQL, false, "999:999", 1L);
    }

    // ── removeEvent ──────────────────────────────────────────────────────────

    @Test
    void removeEvent_callsJdbcWithEventId() {
        mockRepository.removeEvent(1L);

        verify(jdbcTemplate).update(REMOVE_SQL, 1L);
    }

    // ── upsertEvent ──────────────────────────────────────────────────────────

    @Test
    void upsertEvent_existingEvent_callsUpdate() {
        Event event = new Event(1L, false, "UPDATED");
        when(jdbcTemplate.queryForObject(eq(GET_SQL), eq(mockRepository), eq(1L))).thenReturn(event);

        mockRepository.upsertEvent(event);

        verify(jdbcTemplate).update(UPDATE_SQL, false, "UPDATED", 1L);
    }

    // ── mapRow ───────────────────────────────────────────────────────────────

    @Test
    void mapRow_mapsResultSetCorrectly() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("EVENT_ID")).thenReturn(7L);
        when(rs.getBoolean("EVENT_STATUS")).thenReturn(false);
        when(rs.getString("EVENT_SCORE")).thenReturn("7:7");

        Event event = mockRepository.mapRow(rs, 0);

        assertEquals(7L, event.getEventId());
        assertFalse(event.isEventStatus());
        assertEquals("7:7", event.getEventScore());
    }
}
