-- Database: tts

-- DROP DATABASE tts;

CREATE DATABASE tts
    WITH 
    OWNER = tts
    ENCODING = 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE = 'en_US.UTF-8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

GRANT TEMPORARY, CONNECT ON DATABASE tts TO PUBLIC;

GRANT ALL ON DATABASE tts TO tts;

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
    
CREATE SEQUENCE public.words_bk_id_seq
    INCREMENT 1
    START 113
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.words_bk_id_seq
    OWNER TO tts;
