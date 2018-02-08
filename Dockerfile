FROM docker.adeo.no:5000/soknad/soknad-builder:1.1.0
ADD / /workspace

RUN /workspace/build.sh