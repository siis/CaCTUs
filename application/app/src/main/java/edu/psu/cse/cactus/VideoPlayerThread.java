package edu.psu.cse.cactus;

import android.view.Surface;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoPlayerThread extends Thread implements AppConstants {
    private String videoFile;
    private Surface surface;
    private MediaExtractor mediaExtractor;
    private MediaCodec mediaDecoder;

    public VideoPlayerThread(String videoFile, Surface surface) {
        this.videoFile = videoFile;
        this.surface = surface;
    }

    @Override
    public void run() {
        // Create Extractor and pass input file
        mediaExtractor = new MediaExtractor();
        try {
            mediaExtractor.setDataSource(this.videoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // obtain the track and the outputFormat to pass to the decoder
        for (int i = 0; i < mediaExtractor.getTrackCount(); i++) {
            MediaFormat outputFormat = mediaExtractor.getTrackFormat(i);

            String keyMime = outputFormat.getString(MediaFormat.KEY_MIME);
            if (keyMime.startsWith("video/")) {
                mediaExtractor.selectTrack(i);
                try {
                    // Create Decoder
                    mediaDecoder = MediaCodec.createDecoderByType(keyMime);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // And configure it, we pass a surface where the frames will be rendered
                mediaDecoder.configure(outputFormat, surface, null, 0);
                break;
            }
        }

        if (mediaDecoder == null) {
            Log.e("DecodeActivity", "Can't find video info!");
            return;
        }

        //start decoder
        mediaDecoder.start();

        // Variables for the loop
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        boolean endOfStreamReached = false;
        long initTime = System.currentTimeMillis();

        // Now we loop until we are alive
        while (!Thread.interrupted()) {
            // If the end of the video stream has not been reached, we extract frames and feed them to the input Buffer queue
            if (!endOfStreamReached) {
                int inputBufferId = mediaDecoder.dequeueInputBuffer(10000);
                if (inputBufferId >= 0) {
                    ByteBuffer buffer = mediaDecoder.getInputBuffer(inputBufferId);
                    int frameSize = mediaExtractor.readSampleData(buffer, 0);
                    if (frameSize < 0) {
                        // End of file reached, we queue a buffer with a specific flag so that when we dequeue it on the output queue we know that we are done
                        endOfStreamReached = true;
                        mediaDecoder.queueInputBuffer(inputBufferId, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        Log.d("VideoPlayerThread", "End of Stream just reached on inputBuffer side");
                    } else {
                        // Otherwise we queue the frame with correct size and timestamp, and we advance extractor
                        mediaDecoder.queueInputBuffer(inputBufferId, 0, frameSize, mediaExtractor.getSampleTime(), 0);
                        mediaExtractor.advance();
                    }
                }
            }

            // Then we dequeue and see what we can do
            int outputBufferId = mediaDecoder.dequeueOutputBuffer(bufferInfo, 10000);

            if (outputBufferId >= 0){
                // Trick to respect the initial framerate of the video
                while (bufferInfo.presentationTimeUs / 1000L > System.currentTimeMillis() - initTime) {
                    try {
                        sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
                // We can release and display the frame on the surface
                mediaDecoder.releaseOutputBuffer(outputBufferId, true);
            }

            // We have reached end of stream on the outputBuffer queue, we can break the while loop
            if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                Log.d("VideoPlayerThread", "End of stream just reached on outputBuffer side");
                break;
            }
        }

        // Stop and release the codecs
        if (mediaDecoder != null){
            mediaDecoder.stop();
            mediaDecoder.release();
            mediaDecoder = null;
        }
        if(mediaExtractor != null){
            mediaExtractor.release();
            mediaExtractor = null;
        }
    }
}
