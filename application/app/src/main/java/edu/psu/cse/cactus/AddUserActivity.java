package edu.psu.cse.cactus;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.file.Files.readAllBytes;

public class AddUserActivity extends FragmentActivity implements AppConstants{

    FragmentManager fragmentManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDelegation btDelegation;
    private String sha256QRCode;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("phone-lib");
    }
    public native int generateKeyPair();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_delegation);
        fragmentManager = getSupportFragmentManager();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        SharedPreferences appSettings = getSharedPreferences("appSettings", 0);

        Fragment newFragment;

        if (appSettings.getBoolean("alreadySetup", false)) {
            newFragment = new AddUser1Fragment();
        }
        else{
            if (generateKeyPair() != 0){
                new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppAlertTheme))
                        .setIcon(R.drawable.errorimg)
                        .setTitle("Error")
                        .setMessage("Error during the key generation")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finishAndRemoveTask();
                            }
                        }).create().show();
            }
//
//            new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppAlertTheme))
//                    .setIcon(R.drawable.ic_warning)
//                    .setTitle("Warning")
//                    .setMessage("You have to show the QRcode that will be displayed on the next screen to the admin user, ")
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            finishAndRemoveTask();
//                        }
//                    }).create().show();

            newFragment = new BisAddUser1Fragment();
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view, newFragment, null);
        fragmentTransaction.commit();
    }

    public void setBluetoothDelegation(BluetoothDelegation btDelegation){
        this.btDelegation = btDelegation;
    }

    public BluetoothDelegation getBluetoothDelegation(){
        return this.btDelegation;
    }

    public BluetoothAdapter getBluetoothAdapter(){
        return this.bluetoothAdapter;
    }
    public FragmentManager getFragManager(){
        return this.fragmentManager;
    }

    public String getSha256QRCode(){
        return this.sha256QRCode;
    }
    public void setSha256QRCode(String sha256){
        this.sha256QRCode = sha256;
    }

    public String computeSha256PublicKey(){
        //Use SHA-1 algorithm
        MessageDigest shaDigest = null;
        try {
            shaDigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        File publicKey = new File(publicKeyThisPhone);
        //SHA-1 checksum
        String shaChecksum = null;
        try {
            shaChecksum = getFileChecksum(shaDigest, publicKey);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return shaChecksum;
    }

    private static String getFileChecksum(MessageDigest digest, File file) throws IOException
    {
        //Get file input stream for reading the file content
        FileInputStream fis = new FileInputStream(file);
        //Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;
        //Read file data and update in message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        };
        //close the stream; We don't need it now.
        fis.close();
        //Get the hash's bytes
        byte[] bytes = digest.digest();
        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        //return complete hash
        return sb.toString();
    }

    public void writeToBtStreamPublicKey(){
        File file = new File(publicKeyThisPhone);
        Path path = file.toPath();
        byte[] buf = null;
        try {
            buf = readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (buf != null){
            buf = "test".getBytes(Charset.defaultCharset());
            this.btDelegation.write(buf);
        }

    }


}