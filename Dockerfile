FROM navikt/java:11

#COPY /target/sosialhjelp-soknad-api /app
COPY /target/sosialhjelp-soknad-api-boot.jar app.jar
COPY /nais/scripts /init-scripts

ENV JAVA_OPTS="-XX:MaxRAMPercentage=50"
#ENV MAIN_CLASS="no.nav.sosialhjelp.soknad.Application"