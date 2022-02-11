package edu.psu.cse.cactus;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.DialogFragment;

import java.io.File;

public class AlertDialogFragmentFactoryReset extends DialogFragment implements AppConstants {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AppAlertTheme))
                .setIcon(R.drawable.ic_warning)
                .setTitle(R.string.resetTitle)
                .setMessage(R.string.resetMessage)
                .setPositiveButton( R.string.proceedButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //launch factory reset process

                        //check that the folder for the frames exist and empty it
                        File directory = new File(framesFolder);
                        if (directory.exists()) {
                            recursiveDelete(directory);
                        }
                        //check that the video files exist and empty it
                        File video_directory = new File(outputFolder);
                        if (video_directory.exists()) {
                            recursiveDelete(video_directory);
                        }
//                        //check that the key folder exists and empty it
//                        File key_directory = new File(keysFolder);
//                        if (video_directory.exists()) {
//                            recursiveDelete(video_directory);
//                        }

                        // Create object of SharedPreferences.
                        SharedPreferences appSettings = getContext().getSharedPreferences("appSettings", 0);
                        SharedPreferences.Editor editor = appSettings.edit();
                        editor.clear();
                        editor.commit();
                        editor.putBoolean("alreadySetup", false);
                        editor.putLong("seed_leaf_time_inf_in_sec", 1600299654); // todo change
                        editor.commit();

                        getActivity().finish();
                        Intent intent = new Intent(getContext(), MainActivity.class);
                        getContext().startActivity(intent);
                        Toast.makeText(getActivity(), R.string.resetDone, Toast.LENGTH_LONG).show();

                    }
                })
                .setNegativeButton( R.string.cancelButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing
                    }
                }).create();
    }

    public static void recursiveDelete(File file) {
        //to end the recursive loop
        if (!file.exists())
            return;

        //if directory, go inside and call recursively
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                //call recursively
                recursiveDelete(f);
            }
        }
        //call delete to delete files and empty directory
        file.delete();
    }
}
