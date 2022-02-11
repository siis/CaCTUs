package edu.psu.cse.cactus;


public class DownloadFramesThread extends Thread{
    private long t1;
    private long t2;
    private int suffix_frames_folder;
    private int depth_key_tree;
    private int key_rotation_time;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("phone-lib");
    }
    public native int runMainDownloadC(long t1, long t2, int suffix_frames_folder, int depth_key_tree, int key_rotation_time);

    public DownloadFramesThread(long t1, long t2, int suffix_frames_folder, int depth_key_tree, int key_rotation_time){
        this.t1 = t1;
        this.t2 = t2;
        this.suffix_frames_folder = suffix_frames_folder;
        this.depth_key_tree = depth_key_tree;
        this.key_rotation_time = key_rotation_time;
    }

    @Override
    public void run(){
        //call C code to download and decrypt frames

        runMainDownloadC(this.t1, this.t2, this.suffix_frames_folder, this.depth_key_tree, this.key_rotation_time);
    }
}