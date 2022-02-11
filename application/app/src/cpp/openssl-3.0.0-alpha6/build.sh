export ANDROID_NDK_ROOT=/home/ykb5060/Applications/Android/SDK/ndk/21.3.6528147
PATH=$ANDROID_NDK_ROOT/toolchains/llvm/prebuilt/linux-x86_64/bin:$ANDROID_NDK_ROOT/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64/bin:$PATH
#For Nokia 4.2:
#./Configure android-arm64 -D__ANDROID_API__=28
#make

export INSTALL_DIR="`pwd`/build"

# Set this to your minSdkVersion.
export API=28

./Configure android-arm64 -D__ANDROID_API_=$API --prefix=$INSTALL_DIR
make
make install
