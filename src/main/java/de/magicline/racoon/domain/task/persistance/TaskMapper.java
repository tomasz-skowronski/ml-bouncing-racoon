package de.magicline.racoon.domain.task.persistance;

import de.magicline.racoon.domain.task.dto.Task;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper implements RowMapper<Task> {

    @Override
    public Task map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Task(
                rs.getString("task_id"),
                rs.getString("tenant"),
                retrieveInstant(rs.getObject("created_date")),
                retrieveInstant(rs.getObject("modified_date")));
    }

    private Instant retrieveInstant(Object value) {
        Timestamp timestamp = (Timestamp) value;
        return timestamp != null ? timestamp.toInstant() : null;
    }

}
