package edu.psu.cse.cactus;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class Setup2Fragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new2, container, false);

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

        if ( result != null & result.getContents()!= null & result.getContents().length() == 82){
            String qrCOdeText = result.getContents();
            String[] split = qrCOdeText.split(",");
            String macAddress = split[0];

            Toast.makeText(getContext(), "QR Code successfully scanned", Toast.LENGTH_SHORT).show();


            //once feedback received that QRcode was scanned
            FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container_view, new Setup3Fragment(), null);
            fragmentTransaction.commit();
        }
        else {
            // error in qrcode scan or bluetooth pairing show alert message
            AlertDialogFragmentNewBt alertDialog = new AlertDialogFragmentNewBt();
            alertDialog.show(getParentFragmentManager(), "");
        }
    }

}
