FROM navikt/java:11-appdynamics
COPY /web/target/soknadsosialhjelp-server /app
COPY /web/nais/scripts /init-scripts
ENV JAVA_OPTS="-XX:MaxRAMFraction=2"
ENV MAIN_CLASS="no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer"