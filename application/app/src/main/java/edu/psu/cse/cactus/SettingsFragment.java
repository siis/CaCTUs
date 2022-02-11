package edu.psu.cse.cactus;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        configureSettingsButtons(view);
        return view;
    }

    private void configureSettingsButtons(View v) {
        Button delegationBt = v.findViewById(R.id.settingsBt1);
        delegationBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().finish();
                Intent intent = new Intent(getContext(), AddUserActivity.class);
                getContext().startActivity(intent);

            }
        });

        Button factoryBt = v.findViewById(R.id.settingsBt2);
        factoryBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                AlertDialogFragmentFactoryReset alertDialog = new AlertDialogFragmentFactoryReset();
                alertDialog.show(getParentFragmentManager(), "");
            }
        });
    }
}
