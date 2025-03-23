FROM ubuntu:24.04

# Miscelleanous packages
RUN apt-get update && apt-get install -y git \
    build-essential \
    sudo \
    zip \
    unzip \
    curl \
    jq \
    openjdk-21-jdk \
    httpie \
    dos2unix \
    ca-certificates

# Docker CE APT Repository setup

RUN for pkg in docker.io docker-doc docker-compose docker-compose-v2 podman-docker containerd runc; do sudo apt-get remove -y $pkg; done

RUN install -m 0755 -d /etc/apt/keyrings \
    && curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc \
    && chmod a+r /etc/apt/keyrings/docker.asc \
    && echo \
      "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
      $(. /etc/os-release && echo "${UBUNTU_CODENAME:-$VERSION_CODENAME}") stable" | \
     tee /etc/apt/sources.list.d/docker.list > /dev/null 
    
# K6 APT Repository setup
RUN gpg -k \
    && gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69 \
    && echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | tee /etc/apt/sources.list.d/k6.list  

# Install Docker & K6
RUN apt-get update \
        && apt-get install -y docker-ce docker-ce-cli \ 
        containerd.io docker-buildx-plugin docker-compose-plugin \
        k6

# Clean up    
RUN apt-get clean \
    && rm -rf /var/cache/debconf/* /var/lib/apt/lists/* /tmp/* /var/tmp/* \
    && apt-get clean -y

RUN rm /bin/sh && ln -s /bin/bash /bin/sh

# Create the workshop user. UID must be 1002.
ARG USER=workshop
RUN useradd -l -u 1002 -G sudo -md /home/workshop -s /bin/bash -p "$(openssl passwd -1 workshop)" workshop
USER ${USER}
WORKDIR /home/${USER}

# Install SDKMAN
RUN curl -ks "https://get.sdkman.io" | bash

RUN chmod a+x ${HOME}/.sdkman/bin/sdkman-init.sh \
    && source ${HOME}/.sdkman/bin/sdkman-init.sh \
    && sdk install java 21.0.5-tem  \
    && sdk install gradle  \
    && sdk flush 
