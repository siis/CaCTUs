package edu.psu.cse.cactus;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.Timer;
import java.util.TimerTask;

public class BisAddUser4Fragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bisadduser4, container, false);

        TimerTask timerTask = new TimerTask() {
            public void run() {
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container_view, new BisAddUser5Fragment(), null);
                fragmentTransaction.commit();
            }
        };

        new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AppAlertTheme))
                .setTitle("On the admin's device")
                .setMessage("Click on the Next button below the QRcode that you have scanned on the admin's device.")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Timer t = new Timer();
                        t.schedule(timerTask, 40000L);
                    }
                }).create().show();


        return view;
    }

}
