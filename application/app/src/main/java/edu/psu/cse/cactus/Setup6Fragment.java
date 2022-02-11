package edu.psu.cse.cactus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class Setup6Fragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new6, container, false);

        final CheckBox checkBox = view.findViewById(R.id.checkBox);

        SharedPreferences appSettings = getActivity().getSharedPreferences("appSettings", 0);
        String pass_seed= appSettings.getString("recovery_seed", "273608094548640956041245983957");

        int arrayLength = (int) Math.ceil(((pass_seed.length() / (double)5)));
        String[] passwords = new String[arrayLength];

        int j = 0;
        int lastIndex = passwords.length - 1;
        for (int i = 0; i < lastIndex; i++) {
            passwords[i] = pass_seed.substring(j, j + 5);
            j += 5;
        } //Add the last bit
        passwords[lastIndex] = pass_seed.substring(j);

        TextView edit1 = view.findViewById(R.id.editTextNumberPassword1);
        TextView edit2 = view.findViewById(R.id.editTextNumberPassword2);
        TextView edit3 = view.findViewById(R.id.editTextNumberPassword3);
        TextView edit4 = view.findViewById(R.id.editTextNumberPassword4);
        TextView edit5 = view.findViewById(R.id.editTextNumberPassword5);
        TextView edit6 = view.findViewById(R.id.editTextNumberPassword6);

        edit1.setText(passwords[0]);
        edit2.setText(passwords[1]);
        edit3.setText(passwords[2]);
        edit4.setText(passwords[3]);
        edit5.setText(passwords[4]);
        edit6.setText(passwords[5]);

        Button menuNext = view.findViewById(R.id.nextRecovery4);
        menuNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //ask to enable Bluetooth

                if (checkBox.isChecked()){
                    FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container_view, new Setup7Fragment(), null);
                    fragmentTransaction.commit();
                } else{
                    Toast.makeText(getActivity(), R.string.newSystem6Item3, Toast.LENGTH_LONG).show();
                }

            }
        });
        return view;
    }

}
