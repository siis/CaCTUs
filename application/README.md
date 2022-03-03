# Structure

* `app/src/main` contains the Java code of the app, this is the core part of the app
* `app/src/cpp` contains the C code and libraries. They need to be cross compiled, see the corresponding `README.md` file in this folder for details.

To build the application and install it on a smartphone, you need to open this code under a new project in [Android Studio](https://developer.android.com/studio/).

After you have configured correctly the following, you will be able to install the app through Android Studio on a smartphone connected to your machine:

  - In `app/src/cpp/download/src/main.c` set `char *url`, `char *url_index`, and `char *pubkey_camera`. You will need to copy the public key generated for the camera on the smartphon at the location specified by `char *pubkey_camera` (refer to the `README.md` file of the `camera_system` for the generation of this key).
  - Note that the following file also contains a list of default settings:  `app/src/main/java/edu/psu/cse/cactus/AppConstants.java`.
