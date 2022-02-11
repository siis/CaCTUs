C code and C Libraries for application

# Structure

* `curl`: dependency for download
* `download`: C code to download and decrypt frames
* `libconfig`: dependency if reuse of C code of the camera system using a config file
* `openssl3.0`: dependency for download
* `tidy-html5`: dependy for download
* `zlib`: dependency for download

# Cross compilation

See `CMakeLists.txt` and `build.sh` scripts for the different steps used to cross compile each library
