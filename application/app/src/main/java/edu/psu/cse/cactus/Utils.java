package edu.psu.cse.cactus;

import java.io.File;

public class Utils {

    private Utils(){}


    /**
     * @param directory File descriptor of the directory which files have to be deleted
     **/
    public static void deleteFilesInDirectory(File directory){
        String[] children = directory.list();
        for (int i = 0; i < children.length; i++)
        {
            new File(directory, children[i]).delete();
        }
    }


}
