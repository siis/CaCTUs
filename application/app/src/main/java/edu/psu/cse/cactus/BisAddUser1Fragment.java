package edu.psu.cse.cactus;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class BisAddUser1Fragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bisadduser1, container, false);

        ImageView qrDisplay = view.findViewById(R.id.btRecovery1);
        String datoToEncode = ((AddUserActivity)getActivity()).computeSha256PublicKey();

        MultiFormatWriter multiFormatWriter=new MultiFormatWriter();
        try{
            BitMatrix bitMatrix=multiFormatWriter.encode(datoToEncode, BarcodeFormat.QR_CODE,200,200);
            BarcodeEncoder barcodeEncoder=new BarcodeEncoder();
            Bitmap bitmap=barcodeEncoder.createBitmap(bitMatrix);
            qrDisplay.setImageBitmap(bitmap);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        Button nextBt = view.findViewById(R.id.next);
        nextBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentTransaction fragmentTransaction = ((AddUserActivity)getActivity()).getFragManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container_view, new BisAddUser2Fragment(), null);
                fragmentTransaction.commit();
            }
        });
        return view;
    }

}