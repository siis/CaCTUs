C code and C Libraries for application

# Structure

* `curl`: dependency for download
* `download`: C code to download and decrypt frames
* `openssl3.0.2`: dependency for download
* `tidy-html5`: dependency for download
* `zlib`: dependency for download

# Cross compilation

See the `CMakeLists.txt` file and the `cross_compile_libraries.sh` script for the different steps used to cross compile each library. You may need to modify this script based on the target architecture of your smartphone.
