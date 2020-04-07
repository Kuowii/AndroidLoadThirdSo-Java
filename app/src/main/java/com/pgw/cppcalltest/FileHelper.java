package com.pgw.cppcalltest;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {
    public static String TAG = "Main";
    /**
     * 获取目录下所有文件
     * @param path 指定目录路径
     * @return
     */
    public static List<String> getFilesAllName(String path) {
        File file=new File(path);
        File[] files=file.listFiles();
        if (files == null){
            Log.e(TAG, path + " is empty.");
            return null;
        }

        String strTemp = null;
        List<String> s = new ArrayList<>();
        for(int i =0;i<files.length;i++){
            if(files[i].isDirectory())
            {
                // 文件夹则遍历
                s.addAll(getFilesAllName(files[i].getAbsolutePath()));
            }else
            {
                // 文件则加入
                s.add(files[i].getAbsolutePath());
            }
        }
        return s;
    }

    public  static  String createFile(String path)
    {
        File f = new File(path);
        return createFile(f);
    }

    // 创建文件
    public static String createFile(File file){
        try{
            if(file.getParentFile().exists()){
                Log.i(TAG,"----- 创建文件" + file.getAbsolutePath());
                file.createNewFile();
            }
            else {
                //创建目录之后再创建文件
                createDir(file.getParentFile().getAbsolutePath());
                file.createNewFile();
                Log.i(TAG,"----- 创建文件" + file.getAbsolutePath());
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

    public static String createDir(String dirPath){
        //因为文件夹可能有多层，比如:  a/b/c/ff.txt  需要先创建a文件夹，然后b文件夹然后...
        try{
            File file=new File(dirPath);
            if(file.getParentFile().exists()){
                Log.i(TAG,"----- 创建文件夹" + file.getAbsolutePath());
                file.mkdir();
                return file.getAbsolutePath();
            }
            else {
                createDir(file.getParentFile().getAbsolutePath());
                Log.i(TAG,"----- 创建文件夹" + file.getAbsolutePath());
                file.mkdir();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return dirPath;
    }
}
