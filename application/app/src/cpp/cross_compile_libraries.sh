#!/bin/sh

if [ -z $ANDROID_NDK_ROOT ]
then
  echo "Define the Env variable for \$ANDROID_NDK_ROOT (export ANDROID_NDK_ROOT=~/Applications/Android/SDK/ndk/21.3.6528147)"
  exit 1
fi

# Configure and build.
PATH=$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64/bin:$ANDROID_NDK_ROOT/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin:$PATH

# Only choose one of these, depending on your device...  
# The options are aarch64-linux-android, armv7a-linux-androideabi, i686-linux-android, x86_64-linux-android
export TARGET=aarch64-linux-android

# For Nokia 4.2 we need to pick android-arm64 (modify accordingly to your architecture). 
# You have to name your target explicitly; there are android-arm, android-arm64, android-mips, android-mip64, android-x86 and android-x86_64
export TARGET_OPENSSL=android-arm64

# Set this to your minSdkVersion.
export API=28

# Set this to the corresponding names
openssl_folder=openssl
zlib_folder=zlib
curl_folder=curl
tidy_folder=tidy-html5

# Install dir for cross-compiled libraries
export INSTALL_DIR="`pwd`/libs"

export CPPFLAGS="-I${INSTALL_DIR}/include" #path to zlib and openssl header folder
export LDFLAGS="-L${INSTALL_DIR}/lib" #path to zlib and openssl library folder
export SSL_DIR="${INSTALL_DIR}"
export ZLIB_DIR="${INSTALL_DIR}"

# OpenSSL
echo "OpenSSL"
cd $openssl_folder
git checkout openssl-3.0.2
git submodule update --init --recursive
./Configure $TARGET_OPENSSL -D__ANDROID_API_=$API --prefix=$INSTALL_DIR
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
./configure --prefix $INSTALL_DIR
make
make install

cd ..

# curl
echo "curl"
cd $curl_folder
git checkout curl-7_82_0
autoreconf -fi
./configure --host=$TARGET --target=$TARGET --prefix=$INSTALL_DIR --without-ssl --without-zlib --disable-ftp --disable-gopher --disable-imap --disable-ldap --disable-ldaps --disable-pop3 --disable-proxy --disable-rtsp --disable-smtp --disable-telnet --disable-tftp --without-gnutls --without-libidn --without-librtmp --disable-dict
make
make install

cd ..

# tidy
echo "tidy"
cd $tidy_folder
git checkout 5.8.0
cd build/cmake
cmake ../.. -DCMAKE_BUILD_TYPE=Release -DCMAKE_INSTALL_PREFIX=$INSTALL_DIR
make
make install

cd ../..
