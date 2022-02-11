package edu.psu.cse.cactus;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class Setup5Fragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new5, container, false);

        Button menuNext = view.findViewById(R.id.nextRecovery4);
        menuNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                EditText ssid = view.findViewById(R.id.editTextNetworkName);
                EditText password = view.findViewById(R.id.editTextWifiPassword);

                String ssid_text = String.valueOf(ssid.getText());
                String password_text = String.valueOf(password.getText());

                if ( (ssid_text.equals("siis-iot") && password_text.equals("IoT$ECuriTTY") ) || (ssid_text.equals("Sapphire") && password_text.equals("p68hswuid6xn")) ){
                    //then launch adduser2 or alertmessageBt
                    FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container_view, new Setup6Fragment(), null);
                    fragmentTransaction.commit();
                }else{
                    new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppAlertTheme))
                            .setIcon(R.drawable.errorimg)
                            .setTitle("Wifi Connection Error")
                            .setMessage(R.string.newSystem5Error1)
                            .setPositiveButton( R.string.newSystem5Error2, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //start corresponding fragment
                                }
                            })
                            .setNegativeButton( R.string.newSystem5Error3, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    getActivity().finish();
                                    Intent intent = new Intent(getContext(), MainActivity.class);
                                    getContext().startActivity(intent);
                                }
                            }).create().show();
                }
            }
        });
        return view;
    }

}
