export ANDROID_NDK_ROOT=/home/ykb5060/Applications/Android/SDK/ndk/21.3.6528147
export TOOLCHAIN=$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64

# Only choose one of these, depending on your device...
export TARGET=aarch64-linux-android
#export TARGET=armv7a-linux-androideabi
#export TARGET=i686-linux-android
#export TARGET=x86_64-linux-android

export INSTALL_DIR="`pwd`/build"

export CPPFLAGS="-I`pwd`/../zlib/build/include -I`pwd`/../openssl-3.0.0-alpha6/build/include" #path to zlib and openssl header folder
export LDFLAGS="-L`pwd`/../zlib/build/lib -L`pwd`/../openssl-3.0.0-alpha6/build/lib" #path to zlib and openssl library folder
export SSL_DIR="`pwd`/../openssl-3.0.0-alpha6/build"
export ZLIB_DIR="`pwd`/../zlib/build"

# Set this to your minSdkVersion.
export API=28
# Configure and build.
export AR=$TOOLCHAIN/bin/$TARGET-ar
export AS=$TOOLCHAIN/bin/$TARGET-as
export CC=$TOOLCHAIN/bin/$TARGET$API-clang
export CXX=$TOOLCHAIN/bin/$TARGET$API-clang++
export LD=$TOOLCHAIN/bin/$TARGET-ld
export RANLIB=$TOOLCHAIN/bin/$TARGET-ranlib
export STRIP=$TOOLCHAIN/bin/$TARGET-strip

autoreconf -fi
./configure --host=$TARGET --target=$TARGET --prefix=$INSTALL_DIR --without-ssl --without-zlib --disable-ftp --disable-gopher --disable-imap --disable-ldap --disable-ldaps --disable-pop3 --disable-proxy --disable-rtsp --disable-smtp --disable-telnet --disable-tftp --without-gnutls --without-libidn --without-librtmp --disable-dict
make
make install
