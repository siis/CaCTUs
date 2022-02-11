package edu.psu.cse.cactus;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;

public class AlertDialogFragmentNewBt extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppAlertTheme))
                .setIcon(R.drawable.errorimg)
                .setTitle("Bluetooth Connection Error")
                .setMessage(R.string.newSystem2Error1)
                .setPositiveButton( R.string.newSystem2Error2, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //start corresponding fragment
                    }
                })
                .setNegativeButton( R.string.newSystem2Error3, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finish();
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        getContext().startActivity(intent);
                    }
                }).create();
    }
}
