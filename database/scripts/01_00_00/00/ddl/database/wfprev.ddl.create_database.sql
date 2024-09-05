-- Database: wildfire

-- DROP DATABASE "wildfire";

CREATE DATABASE "wildfire"
    WITH 
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

COMMENT ON DATABASE "wildfire"
    IS 'wildfire database containing schemas used by wildfire applications and services.';

GRANT TEMPORARY, CONNECT ON DATABASE "wildfire" TO PUBLIC;