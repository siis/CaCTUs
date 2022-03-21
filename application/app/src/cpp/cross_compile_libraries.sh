#!/bin/sh

if [ -z $ANDROID_NDK_ROOT ]
then
  echo "Define the Env variable for \$ANDROID_NDK_ROOT (export ANDROID_NDK_ROOT=~/Applications/Android/SDK/ndk/21.3.6528147)"
  exit 1
fi

# Configure and build.
PATH=$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64/bin:$ANDROID_NDK_ROOT/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin:$PATH

# Only choose one of these, depending on your device...
export TARGET=aarch64-linux-android
#export TARGET=armv7a-linux-androideabi
#export TARGET=i686-linux-android
#export TARGET=x86_64-linux-android

# Set this to your minSdkVersion.
export API=28

# Set this to the corresponding names
openssl_folder=openssl
zlib_folder=zlib
curl_folder=curl
tidy_folder=tidy-html5

export CPPFLAGS="-I`pwd`/${zlib_folder}/build/include -I`pwd`/${openssl_folder}/build/include" #path to zlib and openssl header folder
export LDFLAGS="-L`pwd`/${zlib_folder}/build/lib -L`pwd`/${openssl_folder}/build/lib" #path to zlib and openssl library folder
export SSL_DIR="`pwd`/${openssl_folder}/build"
export ZLIB_DIR="`pwd`/${zlib_folder}/build"

# OpenSSL
echo "OpenSSL"
cd $openssl_folder
git checkout openssl-3.0.2
git submodule update --init --recursive
mkdir -p ./build
export INSTALL_DIR="`pwd`/build"
# For Nokia 4.2 we need to pick android-arm64 (modify accordingly to your architecture). You have to name your target explicitly; there are android-arm, android-arm64, android-mips, android-mip64, android-x86 and android-x86_64
./Configure android-arm64 -D__ANDROID_API_=$API --prefix=$INSTALL_DIR
make
make install

cd ..

# Configure Exports needed for curl tidy and zlib
export TOOLCHAIN=$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64
export AR=$TOOLCHAIN/bin/$TARGET-ar
export AS=$TOOLCHAIN/bin/$TARGET-as
export CC=$TOOLCHAIN/bin/$TARGET$API-clang
export CXX=$TOOLCHAIN/bin/$TARGET$API-clang++
export LD=$TOOLCHAIN/bin/$TARGET-ld
export RANLIB=$TOOLCHAIN/bin/$TARGET-ranlib
export STRIP=$TOOLCHAIN/bin/$TARGET-strip

# zlib
echo "zlib"
cd $zlib_folder
git checkout v1.2.11
mkdir -p ./build
export INSTALL_DIR="`pwd`/build"
./configure --prefix $INSTALL_DIR
make
make install

cd ..

# curl
echo "curl"
cd $curl_folder
git checkout curl-7_82_0
mkdir -p ./build
autoreconf -fi
./configure --host=$TARGET --target=$TARGET --prefix=$INSTALL_DIR --without-ssl --without-zlib --disable-ftp --disable-gopher --disable-imap --disable-ldap --disable-ldaps --disable-pop3 --disable-proxy --disable-rtsp --disable-smtp --disable-telnet --disable-tftp --without-gnutls --without-libidn --without-librtmp --disable-dict
make
make install


# tidy
echo "tidy"
cd $tidy_folder
git checkout 5.8.0
cd build/cmake
cmake ../.. -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX=$INSTALL_DIR
make
make install

cd ../..
