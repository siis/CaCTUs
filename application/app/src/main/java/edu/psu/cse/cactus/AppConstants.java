package edu.psu.cse.cactus;

import java.util.UUID;

public interface AppConstants {
    String framesFolder = "/data/data/com.example.CaCTUs/frames/";
    String outputFolder = "/data/data/com.example.CaCTUs/files/";
    String outputFile = "/data/data/com.example.CaCTUs/files/video";
    String seedKeyAndIvFile = "/data/data/com.example.CaCTUs/keys/seedKey";
    String keysFolder = "/data/data/com.example.CaCTUs/keys/";
    String publicKeyThisPhone = "/data/data/com.example.CaCTUs/keys/public_key_this_phone.pem";
    String publicKeyOtherPhone = "/data/data/com.example.CaCTUs/keys/public_key_other_phone.pem";
    String MIME_TYPE = "video/avc";
    int WIDTH = 1280;
    int HEIGHT = 720;
    int BIT_RATE = 4000000;
    int FRAMES_PER_SECOND = 25;
    int IFRAME_INTERVAL = 0;

    String APP_NAME = "CaCTUs";
    UUID APP_UUID =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    int BLUETOOTH_DISCOVERY_TIME = 400;

}
