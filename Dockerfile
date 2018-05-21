FROM openjdk:8-jre

COPY build/install/sscs-job-scheduler /opt/app/

WORKDIR /opt/app

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" curl --silent --fail http://localhost:8484/health

EXPOSE 8484

ENTRYPOINT ["/opt/app/bin/sscs-job-scheduler"]
