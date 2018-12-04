FROM docker.adeo.no:5000/pus/maven as builder
WORKDIR /source
ADD / /source
RUN mvn package -DskipTests

FROM navikt/java:8
COPY --from=builder /source/web/target/soknadsosialhjelp-server.jar /app/app.jar
ENV JAVA_OPTS="-Xmx1536m"