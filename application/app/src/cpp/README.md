C code and C Libraries for application

# Structure

* `curl`: dependency for download
* `download`: C code to download and decrypt frames
* `openssl`: dependency for download
* `tidy-html5`: dependency for download
* `zlib`: dependency for download

# Cross compilation

Run the `./cross_compile_libraries.sh` script to cross compile each library. Pay attention to the following:
  - You need to define the Env variable for `$ANDROID_NDK_ROOT` for instance (adapt to your machine): `export ANDROID_NDK_ROOT=~/Applications/Android/SDK/ndk/21.3.6528147`.
  - You also need to make sure that the `$TARGET` and `$TARGET_OPENSSL` variables in the script correspond to the target architecture of your smartphone.

The `CMakeLists.txt` file is used by Android Studio to know which static libraries to include when building the smartphone application. You need to cross compile the libraries first to build the application.