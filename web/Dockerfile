FROM navikt/java:8

WORKDIR /app

COPY target/appassembler /app/
COPY src/main/resources/login.conf /app/login.conf
RUN mkdir /app/data

ENTRYPOINT /app/bin/app