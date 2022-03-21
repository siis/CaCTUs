FROM ubuntu:latest
# Install tools
ENV DEBIAN_FRONTEND=noninteractive 
RUN apt-get update && \
    apt-get install software-properties-common git gcc make python gdb build-essential autoconf valgrind man nano curl wget -y && \
    apt-get install libtool pkg-config libcurl4-gnutls-dev libgcrypt-dev python3-dev python3-distutils texinfo libbluetooth-dev -y && \
    apt-get clean autoclean && \
    apt-get autoremove -y

ENV LATEST_OPENSSL_VERSION="3.0.2"

RUN wget --no-check-certificate -O /tmp/openssl-${LATEST_OPENSSL_VERSION}.tar.gz "https://www.openssl.org/source/openssl-${LATEST_OPENSSL_VERSION}.tar.gz" \
    && tar -xvf /tmp/openssl-${LATEST_OPENSSL_VERSION}.tar.gz -C /tmp/ \
    && cd /tmp/openssl-${LATEST_OPENSSL_VERSION} \
    && ./config \
    && make \
    # && make test \
    && make install \
    && cd .. \
    && rm -rf /tmp/openssl-${LATEST_OPENSSL_VERSION}.tar.gz \ 
    && rm -rf openssl-${OPENSSL_VERSION}

RUN wget --no-check-certificate -O /tmp/libconfig-1.7.2.tar.gz "https://github.com/hyperrealm/libconfig/archive/v1.7.2.tar.gz" \
    && tar -xvf /tmp/libconfig-1.7.2.tar.gz -C /tmp/ \
    && cd /tmp/libconfig-1.7.2 \
    && autoreconf \
    && ./configure \
    && make \
    && make install \
    && cd .. \
    && rm -rf /tmp/libconfig-1.7.2.tar.gz \ 
    && rm -rf /tmp/libconfig-1.7.2

ENV PATH /usr/local/ssl/bin:$PATH
ENV LD_LIBRARY_PATH /workspaces/encryption_module/src/obj:/usr/local/lib
# Enable GDB
RUN mkdir -p /root
RUN touch /root/.gdbinit && \
    echo "set auto-load safe-path /" >> /root/.gdbinit
# Alias nano
RUN echo "alias nano=\"nano -T 4 -i -E\"" >> /root/.bashrc
EXPOSE 6666
VOLUME "/root/project"
WORKDIR "/root/project"
