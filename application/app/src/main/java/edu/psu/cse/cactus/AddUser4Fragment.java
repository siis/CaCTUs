package edu.psu.cse.cactus;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.ikovac.timepickerwithseconds.MyTimePickerDialog;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddUser4Fragment extends Fragment {

    Button next;
    EditText fromDate, fromTime, toDate, toTime;
    private SimpleDateFormat dateFormatter;
    private SimpleDateFormat timeFormatter;

    private Calendar c1;
    private Calendar c2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_adduser4, container, false);

        fromDate = view.findViewById(R.id.editTextAddFromDate);
        fromTime = view.findViewById(R.id.editTextAddFromTime);
        toDate = view.findViewById(R.id.editTextAddToDate);
        toTime = view.findViewById(R.id.editTextAddToTime);
        next = view.findViewById(R.id.nextAddUser4);
        dateFormatter = new SimpleDateFormat("MM-dd-yyyy", Locale.US);
        timeFormatter = new SimpleDateFormat("h:mm:ss a", Locale.US);


        configureTextAndButtons(view);

        return view;
    }

    private void configureTextAndButtons(View v){

        this.c1 = Calendar.getInstance(Locale.US);
        this.c2 = Calendar.getInstance(Locale.US);

        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        c1.set(Calendar.YEAR, year);
                        c1.set(Calendar.MONTH, monthOfYear);
                        c1.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        fromDate.setText(dateFormatter.format(c1.getTime()));
                    }
                }, c1.get(Calendar.YEAR), c1.get(Calendar.MONTH), c1.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        fromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyTimePickerDialog mTimePicker = new MyTimePickerDialog(v.getContext(), new MyTimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(com.ikovac.timepickerwithseconds.TimePicker view, int hourOfDay, int minute, int seconds) {
                        c1.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c1.set(Calendar.MINUTE, minute);
                        c1.set(Calendar.SECOND, seconds);
                        c1.set(Calendar.MILLISECOND, 0);
                        fromTime.setText(timeFormatter.format(c1.getTime()));
                    }
                }, c1.get(Calendar.HOUR_OF_DAY), c1.get(Calendar.MINUTE), c1.get(Calendar.SECOND), false);
                mTimePicker.show();
            }
        });


        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(v.getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        c2.set(Calendar.YEAR, year);
                        c2.set(Calendar.MONTH, monthOfYear);
                        c2.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        toDate.setText(dateFormatter.format(c2.getTime()));
                    }
                }, c2.get(Calendar.YEAR), c2.get(Calendar.MONTH), c2.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });


        toTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyTimePickerDialog mTimePicker = new MyTimePickerDialog(v.getContext(), new MyTimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(com.ikovac.timepickerwithseconds.TimePicker view, int hourOfDay, int minute, int seconds) {
                        c2.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        c2.set(Calendar.MINUTE, minute);
                        c2.set(Calendar.SECOND, seconds);
                        c2.set(Calendar.MILLISECOND, 0);
                        toTime.setText(timeFormatter.format(c2.getTime()));
                    }
                }, c2.get(Calendar.HOUR_OF_DAY), c2.get(Calendar.MINUTE), c2.get(Calendar.SECOND), false);
                mTimePicker.show();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                long t1 = c1.getTimeInMillis();
                long t2 = c2.getTimeInMillis();

                if ( t1 >= t2 ){
                    // Creating alert Dialog
                    new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AppAlertTheme))
                            .setIcon(R.drawable.errorimg)
                            .setTitle("Timestamp Error")
                            .setMessage(R.string.pastDateError)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).create().show();

                }else{
                    SharedPreferences appSettings = getActivity().getSharedPreferences("appSettings", 0);
                    long seed_leaf_time_inf = appSettings.getLong("seed_leaf_time_inf_in_sec", 0) * 1000L;
                    int depth_key_tree = appSettings.getInt("depth_key_tree", 32);
                    int key_rotation_time = appSettings.getInt("key_rotation_time", 10000);


                    if (accessToDecryptionKeys(depth_key_tree, key_rotation_time, seed_leaf_time_inf, t1, t2)){

                        FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.fragment_container_view, new AddUser5Fragment(), null);
                        fragmentTransaction.commit();
                    }else{
                        // Creating alert Dialog
                        new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AppAlertTheme))
                                .setIcon(R.drawable.errorimg)
                                .setTitle("Access Error")
                                .setMessage("You do not have access to the encryption keys for the chosen time period")
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                }).create().show();
                    }

                }
            }
        });

    }

    public boolean accessToDecryptionKeys(int depth_key_tree, int delta_time, long time_inf, long t1, long t2) {

        long time_sup = (long) (time_inf + delta_time * Math.pow(2, depth_key_tree - 1));
        boolean result = true;

        if (!((t1 < time_sup) && (t1 >= time_inf)) || !((t2 <= time_sup) && (t2 >= time_inf))) {
            result = false;
        }
        return result;
    }


}

