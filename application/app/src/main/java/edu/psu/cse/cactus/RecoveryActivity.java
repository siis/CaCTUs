package edu.psu.cse.cactus;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;

public class RecoveryActivity extends FragmentActivity {

    FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recovery);

        fragmentManager = getSupportFragmentManager();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Fragment newFragment;

        newFragment = new Recovery1Fragment();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view, newFragment, null);
        fragmentTransaction.commit();
    }

    public FragmentManager getFragManager(){
        return this.fragmentManager;
    }

}