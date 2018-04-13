FROM navikt/java:8

WORKDIR /app

COPY . /app/
RUN mkdir /app/data
RUN mkdir /app/etc

ENTRYPOINT /app/bin/app
