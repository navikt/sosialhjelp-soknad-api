FROM docker.pkg.github.com/navikt/sosialhjelp-soknad-api/builder:0.1 as builder
WORKDIR /source
ADD / /source
RUN mvn package

FROM navikt/java:8
COPY --from=builder /source/web/target/soknadsosialhjelp-server /app
COPY --from=builder /source/web/nais/scripts /init-scripts
ENV MAIN_CLASS="no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer"