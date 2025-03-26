ARG VERSION=3.4.2

FROM grafana/loki:${VERSION}

COPY local-config.yaml /etc/loki/local-config.yaml

CMD ["-config.file=/etc/loki/local-config.yaml"]