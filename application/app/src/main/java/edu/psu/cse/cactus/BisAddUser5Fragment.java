package edu.psu.cse.cactus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class BisAddUser5Fragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bisadduser5, container, false);

        Button menuBt = view.findViewById(R.id.doneAddUser);
        menuBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences appSettings = getActivity().getSharedPreferences("appSettings", 0);
                SharedPreferences.Editor editor = appSettings.edit();
                editor.putBoolean("alreadySetup", true);
                editor.commit();

                getActivity().finish();
                Intent intent = new Intent(getContext(), MainActivity.class);
                getContext().startActivity(intent);
            }
        });
        return view;
    }

}
