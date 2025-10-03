-- scripts/reset.sql
-- DANGER: Drops *everything* in the public schema
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;

GRANT ALL ON SCHEMA public TO app;
GRANT ALL ON SCHEMA public TO public;
