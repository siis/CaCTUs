package edu.psu.cse.cactus;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class BisAddUser3Fragment extends Fragment implements AppConstants {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_LOCATION = 2;
    private static final int REQUEST_DISCOVERY_BT = 3;
    private FragmentManager fragmentManager;

    private BluetoothAdapter bluetoothAdapter;
    private LocationManager locationManager ;
    BluetoothDelegation bluetoothDelegation;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bisadduser3, container, false);

        this.bluetoothAdapter =  ((AddUserActivity)getActivity()).getBluetoothAdapter();
        this.fragmentManager = ((AddUserActivity)getActivity()).getFragManager();
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        Button menuBt = view.findViewById(R.id.btRecovery1);

        if (bluetoothAdapter.isEnabled()) {
            launchDiscoverable();

        } else{
            menuBt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    if (bluetoothAdapter == null) {
                        // Device doesn't support Bluetooth
                        new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AppAlertTheme))
                                .setIcon(R.drawable.errorimg)
                                .setTitle("Bluetooth Error")
                                .setMessage("Your device doesn't support Bluetooth.")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        getActivity().finishAndRemoveTask();
                                    }
                                }).create().show();

                    }else if (!bluetoothAdapter.isEnabled()) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                }
            });
        }


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            launchDiscoverable();

        } else if ( requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {

            new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AppAlertTheme))
                    .setIcon(R.drawable.errorimg)
                    .setTitle("Bluetooth Error")
                    .setMessage("Bluetooth is not enabled on your device.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().finishAndRemoveTask();
                        }
                    }).create().show();

        } else if (requestCode == REQUEST_ENABLE_LOCATION){
            launchDiscoverable();
        } else if (requestCode == REQUEST_DISCOVERY_BT && resultCode == BLUETOOTH_DISCOVERY_TIME) {

            bluetoothDelegation = new BluetoothDelegation(getContext());

            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container_view, new BisAddUser4Fragment(), null);
            fragmentTransaction.commit();

        } else if ( requestCode == REQUEST_DISCOVERY_BT && resultCode == Activity.RESULT_CANCELED) {

            new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AppAlertTheme))
                    .setIcon(R.drawable.errorimg)
                    .setTitle("Bluetooth Error")
                    .setMessage("Your device is not discoverable.")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            getActivity().finishAndRemoveTask();
                        }
                    }).create().show();
        }
    }

    public void launchDiscoverable(){

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            //if location is not enabled
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Enable GPS location to scan for nearby Bluetooth devices.")
                    .setCancelable(false)
                    .setPositiveButton("Enable", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQUEST_ENABLE_LOCATION);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AppAlertTheme))
                                    .setIcon(R.drawable.errorimg)
                                    .setTitle("Location Error")
                                    .setMessage("GPS location is not enabled on your device.")
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            getActivity().finishAndRemoveTask();
                                        }
                                    }).create().show();
                        }
                    }).create();
            builder.create().show();

        } else {
            Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //Specify how long the device will be discoverable for, in seconds.//
            discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BLUETOOTH_DISCOVERY_TIME);
            startActivityForResult(discoveryIntent, REQUEST_DISCOVERY_BT);
        }

    }

}
