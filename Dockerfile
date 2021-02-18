FROM navikt/java:11-appdynamics
COPY /app/target/sosialhjelp-soknad-api /app
COPY /app/nais/scripts /init-scripts
ENV JAVA_OPTS="-XX:MaxRAMPercentage=50"
ENV MAIN_CLASS="no.nav.sosialhjelp.soknad.web.server.SoknadsosialhjelpServer"