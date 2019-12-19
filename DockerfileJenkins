FROM navikt/java:8
COPY web/target/soknadsosialhjelp-server /app
COPY web/nais/scripts /init-scripts
ENV JAVA_OPTS="-Xmx1536m"
ENV MAIN_CLASS="no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer"