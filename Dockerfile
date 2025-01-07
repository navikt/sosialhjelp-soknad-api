FROM ghcr.io/navikt/baseimages/temurin:21

COPY /build/libs/app.jar app.jar

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/oom-dump.hprof"

RUN java -XX:MaxRAMPercentage=75 -XX:+PrintFlagsFinal -version
