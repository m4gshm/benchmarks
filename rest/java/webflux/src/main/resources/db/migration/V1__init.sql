create TABLE IF NOT EXISTS task
(
    id       text NOT NULL,
    text     text,
    deadline timestamp without time zone,
    PRIMARY KEY (id)
);

create TABLE IF NOT EXISTS task_tag
(
    task_id text NOT NULL
        constraint fk_task_tags references task,
    tag     text NOT NULL,
    PRIMARY KEY (task_id, tag)
);

create index if not exists idx_task_tag_tag on task_tag (tag);