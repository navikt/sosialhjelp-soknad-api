FROM docker.adeo.no:5000/pus/maven as builder
WORKDIR /source
ADD / /source
RUN mvn package -DskipTests

FROM navikt/java:8
COPY --from=builder /source/web/target/soknadsosialhjelp-server /app
ENV JAVA_OPTS="-Xmx1536m"
ENV MAIN_CLASS="no.nav.sbl.dialogarena.server.SoknadsosialhjelpServer"