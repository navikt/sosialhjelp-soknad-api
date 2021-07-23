FROM navikt/java:11-appdynamics
COPY /target/sosialhjelp-soknad-api /app
COPY /nais/scripts /init-scripts
ENV JAVA_OPTS="-XX:MaxRAMPercentage=50"
ENV MAIN_CLASS="no.nav.sosialhjelp.soknad.Application"