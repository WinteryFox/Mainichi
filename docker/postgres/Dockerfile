FROM postgres:13.1-alpine

ENV POSTGRES_PASSWORD 12345

COPY tables.sql /docker-entrypoint-initdb.d/tables.sql
COPY languages.sql /docker-entrypoint-initdb.d/languages.sql
