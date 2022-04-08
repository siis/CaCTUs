This is the code for the Raspberry Pi equipped with a Camera Pi Sensor Module that is used as camera device.

# Code structure

* The `include/` folder contains the `.h` files
* The `src/` folder contains the `.c` files, the Makefile, the `img/` folder where the application temporarily stores the encrypted frames before uploading them, the `keys/` folder where the assymmetric keys of the camera device and the public key of the main user must be placed, and the `obj/` folder where `.o` files will be placed.
* The `.devcontainer/` folder contains the Dockerfile and the settings of the container that you can use for development with VScode.

# Raspberry Pi Camera Setup

## Driver
* Enable the camera sensor: `sudo raspi-config`
* Add `bcm2835-v4l2` to `/etc/modules`
* Check that the v4l2 module has been loaded with: `v4l2-ctl --list-devices` and/or `v4l2-ctl --overlay=1`

## Packages to install

Refer to the `setup_pi.sh` script for the different dependencies to install. 

## Compilation

Use the `make` utility with the provided Makefile:
```
cd src/
make
```

## Config file, assymmetric keys, and execution

Edit `src/config.cfg` with the correct configurations (hostname of the server, camera number, settings to use). This is also where the locations of the public and private keys of the camera device and the public key of the main user are specified. Make sure that the corresponding keys are placed at these locations (in the `src/keys/` folder). 

Then, you can run the application:
```
cd src/
./main
```

Note: a counter is used in `camera.c` to stop the frame capture after some moment, edit the value of this counter or remove it depending on your use case.
# Misc

## Troubleshooting OpenSSL 3.0

You may face the following error, after the installation, after running `openssl version -a`: *openssl: error while loading shared libraries: libssl.so.3: cannot open shared object file: No such file or directory*. This can be solved by copying some files from `/usr/local/lib/` to `/usr/lib/` and creating symlinks:
```
sudo cp /usr/local/lib/libcrypto.so.3 /usr/local/lib/libcrypto.a /usr/local/lib/libssl.so.3 /usr/lib
cd /usr/lib
sudo ln -s libssl.so.3 libssl.so
sudo ln -s libcrypto.so.3 libcrypto.so
```

If you face any error while compiling the code, make sure that you removed libssl1.1:
```
sudo apt-get purge libssl-dev 
sudo apt-get purge libcrypto 
sudo apt-get purge libcrypto-dev
```

In some cases, you might also want to copy paste the header files in the correct location:
```
locate openssl/evp.h
sudo cp /usr/local/include/openssl/* /usr/include/openssl/
```