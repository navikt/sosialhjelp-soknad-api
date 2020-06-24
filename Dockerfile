FROM docker.pkg.github.com/navikt/sosialhjelp-soknad-api/builder:0.2-jdk-11 as builder
WORKDIR /source
ADD / /source
RUN mvn install

FROM navikt/java:11
COPY --from=builder /source/web/target/soknadsosialhjelp-server /app
COPY --from=builder /source/web/nais/scripts /init-scripts
ENV JAVA_OPTS="-XX:MaxRAMFraction=2"
ENV MAIN_CLASS="no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer"