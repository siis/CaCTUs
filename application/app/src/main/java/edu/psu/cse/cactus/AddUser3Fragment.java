package edu.psu.cse.cactus;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;

public class AddUser3Fragment extends Fragment implements AdapterView.OnItemClickListener, AppConstants {
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_ENABLE_LOCATION = 2;

    private static final String TAG = "Delegation";
    private FragmentManager fragmentManager;

    private Button menuBt;
    private TextView titleView;

    private BluetoothAdapter bluetoothAdapter;
    private LocationManager locationManager ;

    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    ListView deviceList;
    BluetoothDevice mBTDevice;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adduser3, container, false);

        this.fragmentManager = ((AddUserActivity)getActivity()).getFragManager();
        this.bluetoothAdapter =  ((AddUserActivity)getActivity()).getBluetoothAdapter();
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);

        //Broadcasts when bond state changes (ie:pairing)
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        requireActivity().registerReceiver(receiverBond, filter);

        deviceList = view.findViewById(R.id.deviceList);
        deviceList.setVisibility(View.GONE);
        menuBt = view.findViewById(R.id.btRecovery1);
        titleView = view.findViewById(R.id.titleDelegation);

//        for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
//            mBTDevices.add(device);
//        }

        mDeviceListAdapter = new DeviceListAdapter(getContext(), R.layout.device_adapter, mBTDevices);
        deviceList.setAdapter(mDeviceListAdapter);
        deviceList.setOnItemClickListener(this);

        if (bluetoothAdapter.isEnabled()) {
            launchDiscovery();

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
            launchDiscovery();

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
            launchDiscovery();
        }
    }

    public void launchDiscovery(){

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
            menuBt.setEnabled(false);
            menuBt.setVisibility(View.GONE);
            deviceList.setVisibility(View.VISIBLE);
            titleView.setText(R.string.addUser1bis);


            if (bluetoothAdapter.isDiscovering()){
                bluetoothAdapter.cancelDiscovery();
            }
            bluetoothAdapter.startDiscovery();
            // Register for broadcasts when a device is discovered.
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            getActivity().registerReceiver(receiver, filter);
        }

    }


    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                String deviceName = device.getName();
//                String deviceHardwareAddress = device.getAddress(); // MAC address
                mBTDevices.add(device);

                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter, mBTDevices);
                deviceList.setAdapter(mDeviceListAdapter);

            }

        }
    };

    /**
     * Broadcast Receiver that detects bond state changes (Pairing status changes)
     */
    private final BroadcastReceiver receiverBond = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bonded already
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    mBTDevice = mDevice;
                    ((AddUserActivity)getActivity()).getBluetoothDelegation().startClient(mBTDevice, APP_UUID);
                    //send public key
//                    System.out.println("send public key");
                    ((AddUserActivity)getActivity()).writeToBtStreamPublicKey();
                    FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container_view, new AddUser4Fragment(), null);
                    fragmentTransaction.commit();
                }
                //case2: creating a bone
                if (mDevice.getBondState() == BluetoothDevice.BOND_BONDING) {
                }
                //case3: breaking a bond
                if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Don't forget to unregister the ACTION_FOUND receiver.
        getActivity().unregisterReceiver(receiver);
        getActivity().unregisterReceiver(receiverBond);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because its very memory intensive.
        bluetoothAdapter.cancelDiscovery();

        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        mBTDevices.get(i).createBond();

        mBTDevice = mBTDevices.get(i);

        ((AddUserActivity)getActivity()).setBluetoothDelegation(new BluetoothDelegation(getContext()));
    }



}
