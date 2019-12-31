CREATE TABLE tasks
(
  task_id       TEXT                     NOT NULL,
  tenant        TEXT                     NOT NULL,
  created_date  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  modified_date TIMESTAMP WITH TIME ZONE
);
