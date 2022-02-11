package edu.psu.cse.cactus;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;

public class AlertDialogFragmentPast extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppAlertTheme))
                .setIcon(R.drawable.errorimg)
                .setTitle("Access Error")
                .setMessage(R.string.pastFootageError)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().finishAndRemoveTask();
                    }
                }).create();
    }
}
