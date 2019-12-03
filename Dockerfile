FROM navikt/sosialhjelp-soknad-api:0.1-jdk-8 as builder
WORKDIR /source
ADD / /source
RUN mvn package

FROM navikt/java:8
COPY --from=builder /source/web/target/soknadsosialhjelp-server /app
COPY --from=builder /source/web/nais/scripts /init-scripts
ENV JAVA_OPTS="-Xmx1536m"
ENV MAIN_CLASS="no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer"