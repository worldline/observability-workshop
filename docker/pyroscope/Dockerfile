ARG VERSION=latest

FROM grafana/pyroscope:${VERSION}

COPY --from=busybox:1.35.0-uclibc /bin/sh /bin/sh

COPY --chown=pyroscope:pyroscope --chmod=755 entrypoint.sh /entrypoint.sh

ENTRYPOINT /entrypoint.sh