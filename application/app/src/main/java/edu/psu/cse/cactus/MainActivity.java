package edu.psu.cse.cactus;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.system.ErrnoException;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;

import static android.system.Os.chmod;


public class MainActivity extends FragmentActivity implements AppConstants {

    FragmentManager fragmentManager;
    DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermissions();

        // Create object of SharedPreferences.
        SharedPreferences appSettings = getSharedPreferences("appSettings", 0);
        SharedPreferences.Editor editor = appSettings.edit();

        if (appSettings.getBoolean("alreadySetup", false)) {
            //System already setup
            setContentView(R.layout.main);
            drawerLayout = findViewById(R.id.drawerLayout);
            fragmentManager = getSupportFragmentManager();
            configureMenuButtons();

            //check that the folder for the frames exist and empty it
            File directory = new File(this.framesFolder);
            if (directory.exists()) {
                recursiveDelete(directory);
            }
            //create directory
            directory.mkdir();
            try {
                chmod(this.framesFolder, 0761);
            } catch (ErrnoException e) {
                e.printStackTrace();
            }
            //check that the video files exist and empty it
            File video_directory = new File(this.outputFolder);
            if (video_directory.exists()) {
                recursiveDelete(video_directory);
            }
            //create directory
            video_directory.mkdir();
            try {
                chmod(this.outputFolder, 0761);
            } catch (ErrnoException e) {
                e.printStackTrace();
            }

            launchLive(this);

        } else {
            //not configured yet
            setContentView(R.layout.activity_main);
            configureWelcomeMenu();

            //edit AppSettings
            editor = appSettings.edit();
//            editor.putBoolean("alreadySetup", true);
            editor.putLong("seed_leaf_time_inf_in_sec", 1600299654);
            editor.putString("recovery_seed", "273608094548640956041245983957"); //27360 80945 48640 95604 12459 83957
            editor.commit();

        }
    }

    public static void recursiveDelete(File file) {
        //to end the recursive loop
        if (!file.exists())
            return;

        //if directory, go inside and call recursively
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                //call recursively
                recursiveDelete(f);
            }
        }
        //call delete to delete files and empty directory
        file.delete();
    }


    private void configureWelcomeMenu(){
        Button welcome1 = findViewById(R.id.welcomeBt1);
        welcome1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(MainActivity.this, SetupActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        Button welcome2 = findViewById(R.id.welcomeBt2);
        welcome2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(MainActivity.this, RecoveryActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });

        Button welcome3 = findViewById(R.id.welcomeBt3);
        welcome3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(MainActivity.this, AddUserActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
    }

    private void configureMenuButtons() {
        ImageButton menuBt = findViewById(R.id.menuBt);
        menuBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });

        ImageButton menuBack = findViewById(R.id.menuBack);
        menuBack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });

        Button liveBt = findViewById(R.id.liveBt);
        liveBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchLive(v.getContext());
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });

        Button pastBt = findViewById(R.id.pastBt);
        pastBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchPast(v.getContext());
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });

        Button settingsBt = findViewById(R.id.settingsBt);
        settingsBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                launchSettings(v.getContext());
                drawerLayout.closeDrawer(Gravity.LEFT);
            }
        });
    }

    private boolean isConnectedToNetwork() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void launchLive(Context context) {
        if (!isConnectedToNetwork()) {
            // Creating alert Dialog
            new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AppAlertTheme))
                    .setIcon(R.drawable.errorimg)
                    .setTitle("Wifi Connection Error")
                    .setMessage("Connect your phone to internet and relaunch the app.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finishAndRemoveTask();
                        }
                    }).create().show();
        } else {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container_view, new LiveFragment(), null);
            fragmentTransaction.commit();
        }
    }

    public void launchPast(Context context) {
        if (!isConnectedToNetwork()) {
            // Creating alert Dialog
            new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AppAlertTheme))
                    .setIcon(R.drawable.errorimg)
                    .setTitle("Wifi Connection Error")
                    .setMessage("Connect your phone to internet and relaunch the app.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finishAndRemoveTask();
                        }
                    }).create().show();
        } else {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container_view, new PastFragment1(), null);
            fragmentTransaction.commit();
        }
    }

    public void launchSettings(Context context) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view, new SettingsFragment(), null);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        final DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
//        } else if (fragmentManager.getBackStackEntryCount() > 0) {
//            fragmentManager.popBackStack();
//            fragmentManager.executePendingTransactions();
        } else {
            super.onBackPressed();
        }
    }

    private void checkPermissions() {
        int permissionCheck = checkSelfPermission("Manifest.permission.INTERNET");
        permissionCheck += checkSelfPermission("Manifest.permission.ACCESS_NETWORK_STATE");
        permissionCheck += checkSelfPermission("Manifest.permission.WRITE_EXTERNAL_STORAGE");
        permissionCheck += checkSelfPermission("Manifest.permission.READ_EXTERNAL_STORAGE");
        permissionCheck += checkSelfPermission("Manifest.permission.CAMERA");
        permissionCheck += checkSelfPermission("Manifest.permission.BLUETOOTH");
        permissionCheck += checkSelfPermission("Manifest.permission.BLUETOOTH_ADMIN");
        permissionCheck += checkSelfPermission("Manifest.permission.ACCESS_BACKGROUND_LOCATION");
        permissionCheck += checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");

        if (permissionCheck != 0) {
            this.requestPermissions(new String[]{Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE,  Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }
    }
}