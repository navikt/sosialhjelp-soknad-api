FROM ghcr.io/navikt/baseimages/temurin:19

USER root

#RUN useradd -ms /bin/bash digisos
#RUN chown -R digisos:digisos /init-scripts

#USER digisos

COPY /build/libs/app.jar app.jar
COPY /nais/scripts /init-scripts

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/oom-dump.hprof"

RUN echo 'java -XX:MaxRAMPercentage=75 -XX:+PrintFlagsFinal -version | grep -Ei "maxheapsize|maxram"' > /init-scripts/0-dump-memory-config.sh