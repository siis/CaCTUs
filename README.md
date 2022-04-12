## Building a Privacy-Preserving Smart Camera System (PETS 2022)
This repository contains the source code of the proof of concept used for the performance evaluation for the following [paper](https://arxiv.org/abs/2201.09338):
```
Building a Privacy-Preserving Smart Camera System
Proceedings on Privacy Enhancing Technologies Symposium (PETS), 2022
Yohan Beugin, Quinn Burke, Blaine Hoak, Ryan Sheatsley, Eric Pauley, Gang Tan, Syed Rafiul Hussain, and Patrick McDaniel
```

To cite the paper: 
```
@inproceedings{beugin_building_2022,
	title = {Building a {Privacy}-{Preserving} {Smart} {Camera} {System}},
	booktitle = {Proceedings on {Privacy} {Enhancing} {Technologies} {Symposium} ({PETS})},
	author = {Beugin, Yohan and Burke, Quinn and Hoak, Blaine and Sheatsley, Ryan and Pauley, Eric and Tan, Gang and Hussain, Syed Rafiul and McDaniel, Patrick},
	month = jul,
	year = {2022},
}
```
---

## Cloning this repository and initializing the submodules

``` 
git clone https://github.com/siis/CaCTUs.git
cd CaCTUs
git submodule update --init --recursive
```
## Experimental Setup

### Camera Device
On a `Raspberry Pi 4 Model B Rev 1.1` (Broadcom BCM2711, 1.5 GHz quad-core Cortex-A72 ARM v7 64-bit, 2GB RAM), we used the [Video4Linux2](https://www.linuxtv.org/downloads/legacy/video4linux/API/V4L2_API/spec-single/v4l2.html) driver to interface with the camera sensor and capture frames that are then encrypted using [OpenSSL3.0](https://www.openssl.org/). The Raspberry Pi Camera Module v2 that we used has a still resolution of 8 Megapixels, a sensor resolution of 3280×2464 pixels, and supports the three following video modes 1080p/30fps, 720p/60fps, and 480p/90fps (respectively video quality and maximum frame rate).

### Android Smartphone
We used a `Nokia 4.2` smartphone with Android 10 on which we have installed the implemented application. In this application, we use C native libraries that we have cross-compiled, and C code to download and decrypt the frames. We leveraged the [MediaCodec class](https://developer.android.com/reference/android/media/MediaCodec) to perform the encoding and decoding of video files, as well as the [Quirc](https://github.com/dlbeer/quirc) and Bluetooth libraries to perform the pairing.

### Cloud Storage
An `AWS EC2 t3.small` instance was used to deploy a Nginx web server. Upon request, we serve the list of encrypted frames that were recorded during the time frame specified in the request.

Note that the web server of CaCTUs does not require any particular hardware requirement (CPU/RAM). As this is a basic web interface, 1vCPU, 512MiB of RAM, and a few GB of storage should be enough. Thus, any instance you can deploy on the cloud platform of your choice and even the minimal option will work. 

---

## Code Organization
The system (and the code) is composed of the three following components, please refer to the corresponding `README.md` file into each folder for more details: 
1. **application:** Android application used for the performance evaluation
2. **camera_system:** the C source code of the application to install on the Raspberry Pi used as the camera device 
3. **server:** configuration files, database, and basic web interface to upload and download the encrypted frames

### Keys Generation

This proof of concept requires 2 pairs of assymmetric keys, one for the smartphone application of the main user and the other for the camera device. Use `openssl` to generate them:

```
openssl genrsa -out private_key_main_user.pem 2048
openssl rsa -in private_key_main_user.pem -pubout -out public_key_main_user.pem

openssl genrsa -out private_key_camera.pem 2048
openssl rsa -in private_key_camera.pem -pubout -out public_key_camera.pem
```

Refer to the `README.md` files of the **application** and **camera_system** to know where to place these keys for these components to work.

### Performance Evaluation

To enable performance logging, refer to the **Performance Evaluation** section of the `README.md` files of the **application**, **camera_system**, and **server**.