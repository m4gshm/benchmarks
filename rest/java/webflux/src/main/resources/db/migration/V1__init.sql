CREATE TABLE IF NOT EXISTS task(
    id character varying(255) NOT NULL,
    deadline timestamp without time zone,
    text character varying(255),
    PRIMARY KEY(id)
);