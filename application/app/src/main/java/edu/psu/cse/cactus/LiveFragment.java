package edu.psu.cse.cactus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.system.ErrnoException;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.io.File;
import static android.system.Os.chmod;


public class LiveFragment extends Fragment implements SurfaceHolder.Callback, AppConstants {

    private RenderFramesToSurfaceThread mPlayer = null;
    private DownloadFramesThread dThread = null;
    private boolean access_to_keys = false;
    private int suffix_frames_folder;
    private TextView timestampView;
    private Handler updateTextHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;

        this.updateTextHandler = new Handler();

        SharedPreferences appSettings = getActivity().getSharedPreferences("appSettings", 0);
        long seed_leaf_time_inf = appSettings.getLong("seed_leaf_time_inf_in_sec", 0) * 1000L;
        int depth_key_tree = appSettings.getInt("depth_key_tree", 32);
        int key_rotation_time = appSettings.getInt("key_rotation_time", 10000);
        long seed_leaf_time_sup = (long) (seed_leaf_time_inf + key_rotation_time * Math.pow(2, depth_key_tree - 1));
        access_to_keys = accessToDecryptionKeys(depth_key_tree, key_rotation_time, seed_leaf_time_inf, System.currentTimeMillis(), seed_leaf_time_sup);

        if (access_to_keys) {

            //Download Frames and Decrypt them
            this.suffix_frames_folder = (int) (Math.random() * 1000);
            this.suffix_frames_folder = checkFolder(this.suffix_frames_folder);

            dThread = new DownloadFramesThread(System.currentTimeMillis(), seed_leaf_time_sup, this.suffix_frames_folder, depth_key_tree, key_rotation_time);
            dThread.start();

            view = inflater.inflate(R.layout.fragment_live, container, false);

            SurfaceView sv = view.findViewById(R.id.surfaceView);
            sv.getHolder().addCallback(this);
            this.timestampView = view.findViewById(R.id.timestamp);
        } else {
            view = inflater.inflate(R.layout.fragment_live_error, container, false);
        }

        return view;
    }

    public int checkFolder(int suffix_folder) {
        File directory = new File(this.framesFolder + suffix_folder);
        if (directory.exists()) {
            suffix_folder = checkFolder((int) (Math.random() * 1000));
        } else {
            //create directory otherwise
            directory.mkdir();
            try {
                chmod(this.framesFolder + suffix_folder, 0761);
            } catch (ErrnoException e) {
                e.printStackTrace();
            }
        }
        return suffix_folder;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mPlayer == null && access_to_keys) {
            // Rebuild the video and play it on Surface Output
            mPlayer = new RenderFramesToSurfaceThread(holder.getSurface(), this.suffix_frames_folder, this.timestampView, this.updateTextHandler);
            mPlayer.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (access_to_keys) {
            File directory = new File(this.framesFolder + this.suffix_frames_folder);
            if (directory.exists()) {
                //we delete all files into it
                Utils.deleteFilesInDirectory(directory);
            }

            if (dThread != null) {
                dThread.interrupt();
            }

            if (mPlayer != null) {
                mPlayer.stopRelease();
                mPlayer.interrupt();
                mPlayer = null;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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