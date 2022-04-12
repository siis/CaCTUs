package edu.psu.cse.cactus;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Handler;
import android.view.Surface;
import android.widget.TextView;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static android.graphics.BitmapFactory.decodeFile;

public class RenderFramesToSurfaceThread extends Thread implements AppConstants {
    private Surface inputSurface;
    private Surface outputSurface;
    private MediaCodec mediaDecoder;
    private MediaCodec mediaEncoder;
    private int useless = 0;
    private int suffix_frames_folder;
    private TextView timestampView;
    private SimpleDateFormat dateFormatter;
    private Handler handler;

    public RenderFramesToSurfaceThread(Surface outputSurface, int suffix_frames_folder, TextView timestampView, Handler handler) {
        this.outputSurface = outputSurface;
        this.suffix_frames_folder = suffix_frames_folder;
        this.timestampView = timestampView;
        this.dateFormatter = new SimpleDateFormat("mm:ss:SSS", Locale.US);
        this.handler = handler;
    }

    @Override
    public void run() {

        // Create correct outputFormat
        MediaFormat outputFormat = MediaFormat.createVideoFormat(this.MIME_TYPE, this.WIDTH, this.HEIGHT);
        outputFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, this.BIT_RATE);
        outputFormat.setInteger(MediaFormat.KEY_FRAME_RATE, this.FRAMES_PER_SECOND);
        outputFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, this.IFRAME_INTERVAL);

        try {
            // Create Decoder and Encoder
            mediaDecoder = MediaCodec.createDecoderByType(outputFormat.getString(MediaFormat.KEY_MIME));
            mediaEncoder = MediaCodec.createEncoderByType(outputFormat.getString(MediaFormat.KEY_MIME));
        } catch (IOException e) {
            return;
        }
        // And configure them, we pass a surface where the frames will be rendered
        mediaDecoder.configure(outputFormat, outputSurface, null, 0);
        mediaEncoder.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        //create inputsurface for encoder
        inputSurface = mediaEncoder.createInputSurface();

        if (mediaDecoder == null || mediaEncoder == null) {
            return;
        }

        //start decoder and encoder
        mediaDecoder.start();
        mediaEncoder.start();

        // Variables for the loop
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        long initTime = System.currentTimeMillis();
        long timestamp = 0;

        // Loop until the output side is done.
        boolean inputDone = false;
        boolean encoderDone = false;
        boolean outputDone = false;
        while (!outputDone) {

            // If we're not done submitting frames, generate a new one and submit it.  The
            // eglSwapBuffers call will block if the input is full.
            if (!inputDone) {
                List<File> frames = getFrames(this.framesFolder + this.suffix_frames_folder);
                int trials = 0;
                while(trials < 500 && frames.size() == 0){
                    trials +=1;
                    try {
                        sleep(1000L / this.FRAMES_PER_SECOND);
                        frames = getFrames(this.framesFolder + this.suffix_frames_folder);
                    } catch (InterruptedException e) {
                        return;
                    }
                }

                if (frames.size() == 0){
                     // Send an empty frame with the end-of-stream flag set.
                    try{
                        mediaEncoder.signalEndOfInputStream();
                        inputDone = true;
                    } catch (Exception e) {
                        return;
                    }

                } else {
                    generateSurfaceFrame(frames.get(0).getAbsolutePath());
                }
            }
            // Assume output is available.  Loop until both assumptions are false.
            boolean decoderOutputAvailable = true;
            boolean encoderOutputAvailable = !encoderDone;
            while (decoderOutputAvailable || encoderOutputAvailable) {
                // Start by draining any pending output from the decoder.  It's important to
                // do this before we try to stuff any more data in.
                int outputDecoderBufferId;
                try {
                    outputDecoderBufferId = mediaDecoder.dequeueOutputBuffer(bufferInfo, 10000);
                } catch (Exception e) {
                    break;
                }
                if (outputDecoderBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    decoderOutputAvailable = false;
                } else if (outputDecoderBufferId >=0) {
                    // Trick to respect the initial framerate of the video
                    while (bufferInfo.presentationTimeUs / 1000L > System.currentTimeMillis() - initTime) {
                        try {
                            sleep(1000L / this.FRAMES_PER_SECOND);
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    if (this.useless < 10){
                        this.useless += 1;
                    }

                    try {
                        // We can release and display the frame on the surface
                        mediaDecoder.releaseOutputBuffer(outputDecoderBufferId, true);

                        if (this.handler != null){
                            final Calendar c = Calendar.getInstance(Locale.US);
                            c.setTimeInMillis(bufferInfo.presentationTimeUs / 1000L);
                            this.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (timestampView != null){
                                        timestampView.setText(dateFormatter.format(c.getTime()));
                                    }
                                }
                            });
                        }


                    } catch (Exception e) {
                        return;
                    }

                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        outputDone = true;
                    }
                }
                if (outputDecoderBufferId != MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // Continue attempts to drain output.
                    continue;
                }
                // Decoder is drained, check to see if we've got a new buffer of output from the encoder.
                if (!encoderDone) {
                    int outputEncoderBufferId;
                    try {
                        outputEncoderBufferId = mediaEncoder.dequeueOutputBuffer(bufferInfo, 10000);
                    } catch (Exception e) {
                        return;
                    }
                    if (outputEncoderBufferId == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // no output available yet
                        encoderOutputAvailable = false;
                    } else if (outputEncoderBufferId >= 0) {
                        ByteBuffer buffer;
                        try {
                            buffer = mediaEncoder.getOutputBuffer(outputEncoderBufferId);
                            // It's usually necessary to adjust the ByteBuffer values to match BufferInfo.
                            buffer.position(bufferInfo.offset);
                            buffer.limit(bufferInfo.offset + bufferInfo.size);
                        } catch (Exception e) {
                            return;
                        }

                        // Get a decoder input buffer, blocking until it's available.  We just
                        // drained the decoder output, so we expect there to be a free input
                        // buffer now or in the near future (i.e. this should never deadlock
                        // if the codec is meeting requirements).
                        //
                        // The first buffer of data we get will have the BUFFER_FLAG_CODEC_CONFIG
                        // flag set; the decoder will see this and finish configuring itself.
                        int inputBufIndex;
                        try {
                            inputBufIndex = mediaDecoder.dequeueInputBuffer(-1);
                            ByteBuffer inputBuf = mediaDecoder.getInputBuffer(inputBufIndex);
                            inputBuf.clear();
                            inputBuf.put(buffer);
                            mediaDecoder.queueInputBuffer(inputBufIndex, 0, bufferInfo.size, timestamp, bufferInfo.flags);
                            timestamp += 1000000L / this.FRAMES_PER_SECOND;
                        } catch (Exception e) {
                            return;
                        }

                        // If everything from the encoder has been passed to the decoder, we
                        // can stop polling the encoder output.  (This just an optimization.)
                        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                            encoderDone = true;
                            encoderOutputAvailable = false;
                        }
                        try{
                            mediaEncoder.releaseOutputBuffer(outputEncoderBufferId, false);
                        } catch (Exception e) {
                            return;
                        }
                    }
                }
            }
        }
        stopRelease();
    }

    public void stopRelease(){
        // Stop and release the codecs
        if (mediaDecoder != null){
            mediaDecoder.stop();
            mediaDecoder.release();
            mediaDecoder = null;
        }
        if (mediaEncoder != null){
            mediaEncoder.stop();
            mediaEncoder.release();
            mediaEncoder = null;
        }
        if (inputSurface != null){
            inputSurface.release();
            inputSurface = null;
        }
        if (outputSurface != null){
            outputSurface.release();
            outputSurface = null;
        }


        if (this.handler != null){
            this.handler.post(new Runnable() {
                @Override
                public void run() {
                    if (timestampView != null){
                        timestampView.setText(R.string.viewDone);
                    }
                }
            });
        }
    }

    private void generateSurfaceFrame(String filename){
        try {
            Canvas canvas = inputSurface.lockCanvas(null);
            //load bitmap
            Bitmap mBitmap = decodeFile(filename, null);
            //delete frame
            new File(filename).delete();
            //draw on canvas
            canvas.drawBitmap(mBitmap, 0,0,null);
            //unlock canvas
            if (this.PERF){
                try {
                    // Performance logging
                    FileWriter myWriter = new FileWriter("/data/data/com.example.CaCTUs/rendering.csv", true);
                    myWriter.write(filename + "," + String.valueOf(System.currentTimeMillis()) + ",\n");
                    myWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            inputSurface.unlockCanvasAndPost(canvas);
        } catch (Exception e){
            return;
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
