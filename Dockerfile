FROM artifactory.dsv.com/es-docker/images/hcp-baseimage-ubi11:1.11.2
WORKDIR /usr/src/app

ARG USERNAME
ARG UID
ARG GID
ARG GROUPNAME


# #TODO maybe like this?
#ENV LANG=en_US.UTF-8 \
#    MIN_JAVA_HEAP_MEM_SIZE=200m \
#    MAX_JAVA_HEAP_MEM_SIZE=4g \
#    MAX_JAVA_THREAD_STACK_MEM_SIZE=64m \
#    JAVA_HEAP_DUMP_OPTIONS="-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/var/log/hdump.hprof" \
#    NSS_WRAPPER_PASSWD= \
#    NSS_WRAPPER_GROUP=

#RUN groupadd --system --gid ${GID} ${GROUPNAME} && \
#    useradd --system --uid ${UID} --gid ${GID} ${USERNAME} && \
#    mkdir /files/ && \
#    chown ${USERNAME}:${GROUPNAME} /files/ && \
#    chmod -R ug+rwx /files/

# insted of:
# https://bugzilla.redhat.com/show_bug.cgi?id=1940902 / https://issues.redhat.com/browse/OPENJDK-335 - # #TODO do we realy need this here?
ENV NSS_WRAPPER_PASSWD=
ENV NSS_WRAPPER_GROUP=

RUN groupadd --system --gid ${GID} ${GROUPNAME} && useradd --system --uid ${UID} --gid ${GID} ${USERNAME}

RUN mkdir /files/
RUN chown ${USERNAME}:${GROUPNAME} /files/
RUN chmod -R ug+rwx /files/

# TODO Przeniesienie polecenia COPY przed zmianą użytkownika na USER ${USERNAME} może pomóc w uniknięciu problemów
# TODO  z uprawnieniami, jeśli plik, który kopiujesz, wymaga uprawnień administratora.
COPY ./target/file-extraction-processor-1.0-jar-with-dependencies.jar file-extraction-processor.jar

USER ${USERNAME}

# TODO Zdefiniowanie zmiennych środowiskowych: Upewnij się, że wszystkie zmienne środowiskowe,
# TODO które są używane w ENTRYPOINT, są zdefiniowane w Dockerfile.
ENTRYPOINT ["java", "-javaagent:/var/tmp/elastic-apm-agent-1.28.1.jar", \
            "-Delastic.apm.enable_log_correlation=true", \
            "-Delastic.apm.service_name=file-extractor-service", \
            "-Delastic.apm.application_packages=com.dsv", \
            "-Delastic.apm.server_url=${ELASTIC_APM_SERVER_URL}", \
            "-jar", "file-extraction-processor.jar"]