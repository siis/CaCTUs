package edu.psu.cse.cactus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import java.util.Timer;
import java.util.TimerTask;

public class Recovery4Fragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recovery4, container, false);

        TimerTask timerTask = new TimerTask() {
            public void run() {
                SharedPreferences appSettings = getActivity().getSharedPreferences("appSettings", 0);
                SharedPreferences.Editor editor = appSettings.edit();
                editor.putBoolean("alreadySetup", true);
                editor.commit();
                getActivity().finish();
                Intent intent = new Intent(getContext(), MainActivity.class);
                getContext().startActivity(intent);
            }
        };

        Timer t = new Timer();
        t.schedule(timerTask, 1000L);

        return view;
    }

}
