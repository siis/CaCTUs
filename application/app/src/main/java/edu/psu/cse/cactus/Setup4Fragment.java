package edu.psu.cse.cactus;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.Timer;
import java.util.TimerTask;

public class Setup4Fragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new4, container, false);


        TimerTask timerTask = new TimerTask() {
            public void run() {
                FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container_view, new Setup5Fragment(), null);
                fragmentTransaction.commit();
            }
        };

        Timer t = new Timer();
        t.schedule(timerTask, 30000L);
        return view;
    }

}
