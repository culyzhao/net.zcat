-- Database: tts

-- DROP DATABASE tts;
CREATE USER tts;

CREATE DATABASE tts
    WITH 
    OWNER = tts
    ENCODING = 'UTF8'
    TEMPLATE = template0
    CONNECTION LIMIT = -1;

GRANT TEMPORARY, CONNECT ON DATABASE tts TO PUBLIC;

GRANT ALL ON DATABASE tts TO tts;

CREATE SEQUENCE public.words_bk_id_seq
    INCREMENT 1
    START 113
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.words_bk_id_seq
    OWNER TO tts;

-- Table: public.words

-- DROP TABLE public.words;

CREATE TABLE public.words
(
    list jsonb
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.words
    OWNER to tts;
    
-- Table: public.words_bk

-- DROP TABLE public.words_bk;

CREATE TABLE public.words_bk
(
    id integer NOT NULL DEFAULT nextval('words_bk_id_seq'::regclass),
    list jsonb,
    CONSTRAINT words_bk_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.words_bk
    OWNER to tts;
    
