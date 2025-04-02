ARG VERSION=11.5.2

FROM grafana/grafana:${VERSION}

COPY dashboards /etc/grafana/provisioning/dashboards
COPY --chown=472:0 entrypoint.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh

ENTRYPOINT /entrypoint.sh