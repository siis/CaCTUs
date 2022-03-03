C code and C Libraries for application

# Structure

* `curl`: dependency for download
* `download`: C code to download and decrypt frames
* `libconfig`: dependency if reuse of C code of the camera system using a config file
* `openssl3.0`: dependency for download
* `tidy-html5`: dependy for download
* `zlib`: dependency for download

# Cross compilation

See the `CMakeLists.txt` file and the `build.sh` scripts inside each dependency folder for the different steps used to cross compile each library. You may need to modify some of these scripts based on the target architecture of your smartphone.
