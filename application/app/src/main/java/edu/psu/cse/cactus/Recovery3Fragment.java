package edu.psu.cse.cactus;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class Recovery3Fragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recovery3, container, false);

        Button nextBt = view.findViewById(R.id.nextRecovery4);

        nextBt.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                EditText edit1 = view.findViewById(R.id.editTextNumberPassword1);
                EditText edit2 = view.findViewById(R.id.editTextNumberPassword2);
                EditText edit3 = view.findViewById(R.id.editTextNumberPassword3);
                EditText edit4 = view.findViewById(R.id.editTextNumberPassword4);
                EditText edit5 = view.findViewById(R.id.editTextNumberPassword5);
                EditText edit6 = view.findViewById(R.id.editTextNumberPassword6);

                String text1 = String.valueOf(edit1.getText());
                String text2 = String.valueOf(edit2.getText());
                String text3 = String.valueOf(edit3.getText());
                String text4 = String.valueOf(edit4.getText());
                String text5 = String.valueOf(edit5.getText());
                String text6 = String.valueOf(edit6.getText());
                String pass_seed = text1 + text2 + text3 + text4 + text5 + text6;

                SharedPreferences appSettings = getActivity().getSharedPreferences("appSettings", 0);
                String pass_correct = appSettings.getString("recovery_seed", "273608094548640956041245983957");
                if ( pass_seed.equals(pass_correct) ){
                    FragmentTransaction fragmentTransaction = ((RecoveryActivity)getActivity()).getFragManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container_view, new Recovery4Fragment(), null);
                    fragmentTransaction.commit();
                }else{
                    new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppAlertTheme))
                            .setIcon(R.drawable.errorimg)
                            .setTitle("Decryption Error")
                            .setMessage(R.string.passwordRecoveryError)
                            .setPositiveButton( R.string.btError1, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //stay here
                                }
                            })
                            .setNegativeButton( R.string.btError2, new DialogInterface.OnClickListener() {
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

