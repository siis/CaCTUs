package edu.psu.cse.cactus;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static android.graphics.BitmapFactory.decodeFile;

public class RenderFramesToFileThread extends Thread implements AppConstants {

    private MediaCodec.BufferInfo bufferInfo;
    private MediaCodec mediaEncoder;
    private MediaMuxer mediaMuxer;
    private Surface mediaInputSurface;

    private int mTrackIndex;
    private boolean mMuxerStarted;
    private long mFakePts = 0;

    private int suffix_frames_folder;

    private List<File> frames;

    //constructor
    public RenderFramesToFileThread(int suffix_frames_folder){
        this.suffix_frames_folder = suffix_frames_folder;
        this.frames = getFrames(this.framesFolder + this.suffix_frames_folder + "/");
    }

    //start encoding and decoding
    @Override
    public void run() {

        File outputFile = new File(this.outputFile + this.suffix_frames_folder + ".mp4");
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

    }

    //recursive call to drain and generate while there are frames that are downloaded
    private void drainAndGenerate() throws InterruptedException {
        int trials = 0;
        this.frames = getFrames(this.framesFolder + this.suffix_frames_folder + "/");

        while( (trials < 100) && (this.frames.size() == 0)){
            trials +=1;
            try {
                System.out.println("waiting");
                sleep(1000L / this.FRAMES_PER_SECOND);
                this.frames = getFrames(this.framesFolder + this.suffix_frames_folder + "/");
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
            System.out.println(filename);
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

