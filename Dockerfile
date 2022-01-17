FROM navikt/java:11

COPY /target/sosialhjelp-soknad-api.jar app.jar
COPY /nais/scripts /init-scripts

ENV JAVA_OPTS="-XX:MaxRAMPercentage=50"