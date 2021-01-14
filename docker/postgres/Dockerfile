FROM postgres:13.1

ENV POSTGRES_PASSWORD=12345

COPY snowflake/snowflake--1.0.0.sql /usr/share/postgresql/13/extension
COPY snowflake/snowflake.control /usr/share/postgresql/13/extension
COPY tables.sql /docker-entrypoint-initdb.d/tables.sql
COPY languages.sql /docker-entrypoint-initdb.d/languages.sql
