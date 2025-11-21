-- Database: tts

-- DROP DATABASE IF EXISTS tts;

CREATE DATABASE tts
    WITH
    OWNER = tts
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.utf8'
    LC_CTYPE = 'en_US.utf8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1
    IS_TEMPLATE = False;

GRANT TEMPORARY, CONNECT ON DATABASE tts TO PUBLIC;

GRANT ALL ON DATABASE tts TO tts;

-- Table: public.high

-- DROP TABLE IF EXISTS public.high;

CREATE TABLE IF NOT EXISTS public.high
(
    id integer NOT NULL GENERATED ALWAYS AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    word character varying(100) COLLATE pg_catalog."default" NOT NULL,
    pron character varying(100) COLLATE pg_catalog."default" NOT NULL,
    trans character varying(400) COLLATE pg_catalog."default" NOT NULL,
    stat integer,
    times integer,
    CONSTRAINT high_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.high
    OWNER to tts;
