package edu.psu.cse.cactus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Handler;
import android.system.ErrnoException;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.io.File;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static android.graphics.BitmapFactory.decodeFile;
import static android.system.Os.chmod;
import static java.lang.Thread.sleep;

public class PastFragment2 extends Fragment implements SurfaceHolder.Callback, AppConstants {

    private RenderFramesToSurfaceThread mPlayer = null;
    private RenderFramesToFileThread mRenderer = null;
    private DownloadFramesThread dThreadFile = null;
    private DownloadFramesThread dThread = null;
    private static final int CREATE_VIDEO_FILE = 1;
    private int suffix_frames_folder;
    private int suffix_video_file;
    private long t1;
    private long t2;
    private SimpleDateFormat dateFormatter;
    private TextView timestampView;
    private Handler updateTextHandler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;

        Bundle bundle = this.getArguments();

        if (bundle != null){
            view = inflater.inflate(R.layout.fragment_past2, container, false);

            this.t1 = bundle.getLong("t1", 0);
            this.t2 = bundle.getLong("t2", 0);
            dateFormatter = new SimpleDateFormat("MM-dd-yyyy h:mm:ss a", Locale.US);
            TextView fromTimestamp = view.findViewById(R.id.fromPastFootage);
            TextView toTimestamp = view.findViewById(R.id.toPastFootage);
            fromTimestamp.setText(dateFormatter.format(t1));
            toTimestamp.setText(dateFormatter.format(t2));

            SharedPreferences appSettings = getActivity().getSharedPreferences("appSettings", 0);
            int depth_key_tree = appSettings.getInt("depth_key_tree", 32);
            int key_rotation_time = appSettings.getInt("key_rotation_time", 10000);

            //Download Frames and Decrypt them to render to a surface
            this.suffix_frames_folder = (int) (Math.random() * 1000);
            this.suffix_frames_folder = checkFolder(this.suffix_frames_folder);

            dThread = new DownloadFramesThread(t1, t2, this.suffix_frames_folder, depth_key_tree, key_rotation_time);
            dThread.start();

            //Download Frames and Decrypt them to render them to a file
            this.suffix_video_file = (int) (Math.random() * 1000);
            this.suffix_video_file = checkFolder(this.suffix_video_file);

            dThreadFile = new DownloadFramesThread(t1, t2, this.suffix_video_file, depth_key_tree, key_rotation_time);
            dThreadFile.start();

//            mRenderer = new RenderFramesToFileThread(suffix_video_file);
//            mRenderer.start();

            SurfaceView sv = view.findViewById(R.id.surfaceView);
            sv.getHolder().addCallback(this);

            this.updateTextHandler = new Handler();
            this.timestampView = view.findViewById(R.id.timestamp);

            Button returnBt = view.findViewById(R.id.returnFootage);
            returnBt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Fragment pastFragment1 = new PastFragment1();
                    pastFragment1.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = getParentFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_container_view, pastFragment1, null);
                    fragmentTransaction.commit();
                }

            });

            ImageButton downloadBt = view.findViewById(R.id.downloadBt);
            ImageButton shareBt = view.findViewById(R.id.shareBt);

            downloadBt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AppAlertTheme))
                            .setIcon(R.drawable.errorimg)
                            .setTitle("Saving warning")
                            .setMessage("The video file will be saved unencrypted.")
                            .setPositiveButton("I understand", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    File video = new File(getContext().getFilesDir() + "/video" + suffix_video_file + ".mp4");
                                    if (!video.exists()){
                                        RenderFramesToFileAsyncTask runner = new RenderFramesToFileAsyncTask();
                                        runner.execute(true);
                                    }else{
                                        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                                        intent.setType("video/*");
                                        intent.putExtra(Intent.EXTRA_TITLE, "video.mp4");
                                        startActivityForResult(intent, CREATE_VIDEO_FILE);
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null).create().show();

                }
            });

            shareBt.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    new AlertDialog.Builder(new ContextThemeWrapper(getContext(), R.style.AppAlertTheme))
                            .setIcon(R.drawable.errorimg)
                            .setTitle("Sharing warning")
                            .setMessage("The video file will be shared unencrypted.")
                            .setPositiveButton("I understand", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    File video = new File(getContext().getFilesDir() + "/video" + suffix_video_file + ".mp4");
                                    if (!video.exists()){
                                        RenderFramesToFileAsyncTask runner = new RenderFramesToFileAsyncTask();
                                        runner.execute(false);
                                    }else {
                                        Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", new File(getContext().getFilesDir() + "/video" + suffix_video_file + ".mp4"));

                                        Intent shareIntent = new Intent();
                                        shareIntent.setAction(Intent.ACTION_SEND);
                                        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                                        shareIntent.setType("video/*");
                                        shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        Intent chooser = Intent.createChooser(shareIntent, "Share Video");
                                        List<ResolveInfo> resInfoList = getContext().getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
                                        for (ResolveInfo resolveInfo : resInfoList) {
                                            String packageName = resolveInfo.activityInfo.packageName;
                                            getContext().grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        }
                                        startActivity(chooser);
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null).create().show();
                }
            });

        }else{
            view = inflater.inflate(R.layout.fragment_past2_error, container, false);
        }

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == CREATE_VIDEO_FILE && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            File sourceFile = new File(getContext().getFilesDir() + "/video" + suffix_video_file + ".mp4");
            if (resultData != null) {
                Uri outputUri = resultData.getData();

                try {
                    InputStream in = new FileInputStream(sourceFile);
                    OutputStream out = this.getContext().getContentResolver().openOutputStream(outputUri);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.close();
                    Toast.makeText(getActivity(), R.string.videoSaved, Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
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
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mPlayer == null) {
            // Rebuild the video and play it on Surface Output
            mPlayer = new RenderFramesToSurfaceThread(holder.getSurface(), this.suffix_frames_folder, this.timestampView,this.updateTextHandler);
            mPlayer.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mPlayer != null) {
            mPlayer.interrupt();
        }
    }



    //argument: pass true for downloading, false for sharing
    private class RenderFramesToFileAsyncTask extends AsyncTask<Boolean, Void, Void> implements AppConstants {

        private MediaCodec.BufferInfo bufferInfo;
        private MediaCodec mediaEncoder;
        private MediaMuxer mediaMuxer;
        private Surface mediaInputSurface;

        private int mTrackIndex;
        private boolean mMuxerStarted;
        private long mFakePts = 0;

        private List<File> frames;

        ProgressDialog progressDialog;

        private boolean download;

        @Override
        protected Void doInBackground(Boolean... params) {

            this.download = params[0];
            this.frames = getFrames(this.framesFolder + suffix_video_file + "/");


            File outputFile = new File(this.outputFile + suffix_video_file + ".mp4");
            bufferInfo = new MediaCodec.BufferInfo();

            MediaFormat outputFormat = MediaFormat.createVideoFormat(this.MIME_TYPE, this.WIDTH, this.HEIGHT);
            outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, this.BIT_RATE);
            outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, this.FRAMES_PER_SECOND);
            outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, this.IFRAME_INTERVAL);

            // Create Encoder
            try {
                mediaEncoder = MediaCodec.createEncoderByType(this.MIME_TYPE);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //add surface here to output to if needed
            mediaEncoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            //create input surface instead of input buffer
            mediaInputSurface = mediaEncoder.createInputSurface();
            mediaEncoder.start();

            try {
                // create mediaMuxer but can not be started yet because we need the corresponding mediaFormat
                mediaMuxer = new MediaMuxer(outputFile.toString(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mTrackIndex = -1;
            mMuxerStarted = false;


            try {
                drainAndGenerate();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Send end-of-stream and drain remaining output.
            drainEncoder(true);

            // Stop and release codecs and Surface
            if (mediaEncoder != null) {
                mediaEncoder.stop();
                mediaEncoder.release();
                mediaEncoder = null;
            }
            if (mediaInputSurface != null) {
                mediaInputSurface.release();
                mediaInputSurface = null;
            }
            if (mediaMuxer != null) {
                mediaMuxer.stop();
                mediaMuxer.release();
                mediaMuxer = null;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getContext(), getResources().getString(R.string.renderingTitle), getResources().getString(R.string.rendering));
        }


        @Override
        protected void onPostExecute(Void params) {
            progressDialog.dismiss();

            if (this.download){
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("video/*");
                intent.putExtra(Intent.EXTRA_TITLE, "video.mp4");
                startActivityForResult(intent, CREATE_VIDEO_FILE);
            }else{
                Uri fileUri = FileProvider.getUriForFile(getContext(), getContext().getApplicationContext().getPackageName() + ".provider", new File(getContext().getFilesDir() + "/video" + suffix_video_file + ".mp4"));

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                shareIntent.setType("video/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Intent chooser = Intent.createChooser(shareIntent, "Share Video");
                List<ResolveInfo> resInfoList = getContext().getPackageManager().queryIntentActivities(chooser, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo resolveInfo : resInfoList) {
                    String packageName = resolveInfo.activityInfo.packageName;
                    getContext().grantUriPermission(packageName, fileUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }
                startActivity(chooser);
            }

        }

        //recursive call to drain and generate while there are frames that are downloaded
        private void drainAndGenerate() throws InterruptedException {
            int trials = 0;
            this.frames = getFrames(this.framesFolder + suffix_video_file + "/");

            while( (trials < 100) && (this.frames.size() == 0)){
                trials +=1;
                try {
                    sleep(1000L / this.FRAMES_PER_SECOND);
                    this.frames = getFrames(this.framesFolder + suffix_video_file + "/");
                } catch (InterruptedException e) {
                    return;
                }
            }
            final int NUM_FRAMES = this.frames.size();

            if (NUM_FRAMES != 0){
                for (int i = 0; i < NUM_FRAMES; i++) {
                    // Drain any data from the encoder into the muxer.
                    drainEncoder(false);
                    // Generate a frame
                    generateFrame(i);
                }
                drainAndGenerate();
            }
        }

        private void drainEncoder(boolean endOfStream) {
            final int TIMEOUT_USEC = 10000;

            if (endOfStream) {
                mediaEncoder.signalEndOfInputStream();
            }

            while (true) {
                int outputBufferId = mediaEncoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                if (outputBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    if (!endOfStream) {
                        break;      // out of while
                    }
                } else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // should happen before receiving buffers, and should only happen once
                    if (mMuxerStarted) {
                        throw new RuntimeException("format changed twice");
                    }
                    MediaFormat newFormat = mediaEncoder.getOutputFormat();

                    // now that we have the Magic Goodies, start the muxer
                    mTrackIndex = mediaMuxer.addTrack(newFormat);
                    mediaMuxer.start();
                    mMuxerStarted = true;
                } else if (outputBufferId < 0) {
                    // let's ignore it
                } else {
                    ByteBuffer encodedData = mediaEncoder.getOutputBuffer(outputBufferId);
                    if (encodedData == null) {
                        throw new RuntimeException("encoderOutputBuffer " + outputBufferId + " was null");
                    }

                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        // The codec config data was pulled out and fed to the muxer when we got
                        // the INFO_OUTPUT_FORMAT_CHANGED status.  Ignore it.
                        bufferInfo.size = 0;
                    }

                    if (bufferInfo.size != 0) {
                        if (!mMuxerStarted) {
                            throw new RuntimeException("muxer hasn't started");
                        }

                        // adjust the ByteBuffer values to match BufferInfo
                        encodedData.position(bufferInfo.offset);
                        encodedData.limit(bufferInfo.offset + bufferInfo.size);
                        bufferInfo.presentationTimeUs = mFakePts;
                        mFakePts += 1000000L / FRAMES_PER_SECOND;

                        mediaMuxer.writeSampleData(mTrackIndex, encodedData, bufferInfo);
                    }

                    mediaEncoder.releaseOutputBuffer(outputBufferId, false);

                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break;      // out of while
                    }
                }
            }
        }

        private void generateFrame(int frameNum) {
            //lock canvas
            Canvas canvas = mediaInputSurface.lockCanvas(null);
            try {
                //retrieve filename of current frame
                String filename = this.frames.get(frameNum).getAbsolutePath();
                //load bitmap
                Bitmap mBitmap = decodeFile(filename, null);
                //delete frame
                new File(filename).delete();
                //draw on canvas
                canvas.drawBitmap(mBitmap, 0,0,null);
            } finally {
                //unlock canvas
                mediaInputSurface.unlockCanvasAndPost(canvas);
            }
        }

        private List<File> getFrames(String framesFolder) {
            File f = new File(framesFolder);
            File unsortedArray[] = f.listFiles();
            Arrays.sort(unsortedArray, new Comparator<File>() {
                @Override
                public int compare(File object1, File object2) {
                    return object1.getName().compareTo(object2.getName());
                }
            });
            List<File> files = Arrays.asList(unsortedArray);
            return files;
        }
    }
}
