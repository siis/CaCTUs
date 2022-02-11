package edu.psu.cse.cactus;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class Setup1Fragment extends Fragment {
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new1, container, false);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Button menuBt = view.findViewById(R.id.btRecovery1);
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

                }else{
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    } else{
                        // Bluetooth enabled
                        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container_view, new Setup2Fragment(), null);
                        fragmentTransaction.commit();
                    }
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            // Bluetooth enabled
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container_view, new Setup2Fragment(), null);
            fragmentTransaction.commit();
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
        }
    }


}