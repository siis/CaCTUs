package edu.psu.cse.cactus;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class AddUser1Fragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adduser1, container, false);

        new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AppAlertTheme))
                .setTitle("On the added's user device")
                .setMessage("Select the option to access the system as a shared user on the added's user device.")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create().show();

        Button scanButton = view.findViewById(R.id.btRecovery1);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    openScanner();
            }
        });
        return view;
    }

    public void openScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this.getActivity()).forSupportFragment(this);

        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setOrientationLocked(true);
        integrator.setCameraId(0); // Use a specific camera of the device
        integrator.setPrompt("Scan a QR Code");
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(true);
        integrator.setCaptureActivity(ScanPortraitActivity.class);
        integrator.initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);

        if ( result != null & result.getContents()!= null & result.getContents().length() == 64){
            ((AddUserActivity)getActivity()).setSha256QRCode(result.getContents());
            Toast.makeText(getContext(), "QR Code successfully scanned", Toast.LENGTH_SHORT).show();

            new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AppAlertTheme))
                    .setTitle("On the added's user device")
                    .setMessage("Click on the Next button below the QRcode that you just scanned on the added's user device.")
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //once feedback received that QRcode was scanned
                            FragmentTransaction fragmentTransaction = ((AddUserActivity)getActivity()).getFragManager().beginTransaction();
                            fragmentTransaction.replace(R.id.fragment_container_view, new AddUser2Fragment(), null);
                            fragmentTransaction.commit();
                        }
                    }).create().show();


        }
        else {
            new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AppAlertTheme))
                    .setIcon(R.drawable.errorimg)
                    .setTitle("QR Code Scan Error")
                    .setMessage("An error occurred when scanning the QR Code")
                    .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create().show();
        }
    }

}
