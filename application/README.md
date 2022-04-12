# Structure

* `app/src/main/` contains the Java code of the app, this is the core part of the app
* `app/src/cpp/` contains the C code and libraries. They need to be cross compiled.


# To build the smartphone application

1. To build the application and install it on a smartphone, you need to open this code under a new project in [Android Studio](https://developer.android.com/studio/).
   
2. Cross compile the C code and libraries in `app/src/cpp/`. See the corresponding `README.md` file in this folder for details.

3. Connect the smartphone to your machine and allow access to Android Studio. Through Android Studio, open the Device Explorer and create the following:
  - `/data/data/com.example.CaCTUs/frames/`: the folder where the application will temporarily place the decrypted frames before being processed by the renderer.
  - `/data/data/com.example.CaCTUs/files/`: the folder where the application will export videos on user's demand
  - `/data/data/com.example.CaCTUs/keys/`: the folder where the different asymmetric keys will be placed.

4. Configure the following:
  - In `app/src/cpp/download/src/main.c` set `char *url`, `char *url_index`, and `char *pubkey_camera`. You will need to copy the public key generated for the camera on the smartphone at the location specified by `char *pubkey_camera`.
  - Note that the following file also contains a list of default settings:  `app/src/main/java/edu/psu/cse/cactus/AppConstants.java`.

5. Install the application through Android Studio on your smartphone.

# Performance Evaluation

To enable performance logging, set `PERF` to `true` in  `app/src/main/java/edu/psu/cse/cactus/AppConstants.java`.
Then, performance metrics will be saved to the `/data/data/com.example.CaCTUs/rendering.csv` and `/data/data/com.example.CaCTUs/phone.csv` logs.

