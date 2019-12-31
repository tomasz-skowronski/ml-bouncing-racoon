package de.magicline.racoon.domain.task.persistance;

import de.magicline.racoon.domain.task.dto.Task;

import java.util.Optional;

import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface TaskRepository {

    @SqlUpdate("INSERT INTO TASKS (task_id, tenant) VALUES (:taskId, :tenant)")
    void insert(@BindBean Task task);

    @SqlUpdate("UPDATE TASKS SET modified_date=:modifiedDate WHERE task_id=:taskId")
    void update(@BindBean Task task);

    @SqlQuery("SELECT * FROM tasks WHERE task_id=:taskId")
    Optional<Task> findByTaskId(@Bind("taskId") String taskId);

}
