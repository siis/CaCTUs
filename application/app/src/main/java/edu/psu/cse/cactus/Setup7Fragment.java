package edu.psu.cse.cactus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class Setup7Fragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new7, container, false);

        Button menuDone = view.findViewById(R.id.doneAddUser);
        menuDone.setOnClickListener(new View.OnClickListener() {
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
