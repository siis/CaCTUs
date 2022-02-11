package edu.psu.cse.cactus;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.Timer;
import java.util.TimerTask;

public class Setup3Fragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new3, container, false);

        ImageView qrDisplay = view.findViewById(R.id.btRecovery1);
        String datoToEncode = ((SetupActivity)getActivity()).computeSha256PublicKey();

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

        TimerTask timerTask = new TimerTask() {
            public void run() {
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container_view, new Setup4Fragment(), null);
                fragmentTransaction.commit();
            }
        };

        Timer t = new Timer();
        t.schedule(timerTask, 7000L);

        return view;
    }

}
