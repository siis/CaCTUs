#!/bin/sh

# Install necessary packages
sudo apt-get install screen software-properties-common git gcc make gdb build-essential autoconf man nano curl wget libtool pkg-config libcurl4-gnutls-dev libgcrypt-dev texinfo libbluetooth-dev

# OpenSSL 3 install
export LATEST_OPENSSL_VERSION="3.0.2"

wget --no-check-certificate -O /tmp/openssl-${LATEST_OPENSSL_VERSION}.tar.gz "https://www.openssl.org/source/openssl-${LATEST_OPENSSL_VERSION}.tar.gz"
tar -xvf /tmp/openssl-${LATEST_OPENSSL_VERSION}.tar.gz -C /tmp/
cd /tmp/openssl-${LATEST_OPENSSL_VERSION}
./config
make
# && make test \
sudo make install
cd .. \
rm -rf /tmp/openssl-${LATEST_OPENSSL_VERSION}.tar.gz
rm -rf openssl-${OPENSSL_VERSION}

# libconfig install
wget --no-check-certificate -O /tmp/libconfig-1.7.2.tar.gz "https://github.com/hyperrealm/libconfig/archive/v1.7.2.tar.gz"
tar -xvf /tmp/libconfig-1.7.2.tar.gz -C /tmp/
cd /tmp/libconfig-1.7.2
autoreconf
./configure
make
sudo make install
cd ..
rm -rf /tmp/libconfig-1.7.2.tar.gz
rm -rf /tmp/libconfig-1.7.2

# Misc & symlinks
sudo cp /usr/local/lib/libcrypto.so.3 /usr/local/lib/libcrypto.a /usr/local/lib/libssl.so.3 /usr/lib
cd /usr/lib
sudo ln -s libssl.so.3 libssl.so
sudo ln -s libcrypto.so.3 libcrypto.so

# Purge of any potential previous version of OpenSSL already installed on the machine
sudo apt-get purge libssl-dev 
sudo apt-get purge libcrypto 
sudo apt-get purge libcrypto-dev
