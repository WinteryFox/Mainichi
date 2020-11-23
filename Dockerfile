FROM postgres:13.1

ENV POSTGRES_PASSWORD=12345

COPY ./tables.sql /docker-entrypoint-initdb.d