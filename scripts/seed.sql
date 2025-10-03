CREATE TABLE IF NOT EXISTS demo_users (
                                          id SERIAL PRIMARY KEY,
                                          name TEXT,
                                          email TEXT UNIQUE
);

INSERT INTO demo_users (name, email) VALUES
                                         ('Alice', 'alice@example.com'),
                                         ('Bob', 'bob@example.com'),
                                         ('Charlie', 'charlie@example.com')
    ON CONFLICT (email) DO NOTHING;

Quick verifgication:
SELECT * FROM demo_users;