package com.pgw.cppcalltest;

import android.os.AsyncTask;
import android.os.Environment;

import java.util.List;

public class DealFilesAsyncTask extends AsyncTask< String, Integer,Integer> {

    MainActivity ins = null;
    // 作用：接收输入参数、执行任务中的耗时操作、返回 线程任务执行的结果
    @Override
    protected Integer doInBackground(String ...params) {

        int size = 0;
        try {
            if(ins == null)
            {
                ins = MainActivity.GetInstance();
            }

            // 不允许在 doInBackground 中更新UI , 所以UI更新使用 publishProgress 进行发送处理
            publishProgress(1,0);
            List<String> Files = FileHelper.getFilesAllName(params[0]);
            //List<String> Files = params[0];
            size = Files.size();
            publishProgress(2,size);

            String newRootPath = Environment.getExternalStorageDirectory() + "/Pictures/Data/New";
            for(int i =0;i<size;i++)
            {
                String old = Files.get(i);
                String parentPath = old.substring(old.lastIndexOf("Origin") + 6);
                String newFile = newRootPath +parentPath;
                FileHelper.createFile(newFile);

                int r = ins.decryFile(old,newFile,ins.tPoint);
                if(r > 0)
                {
                    publishProgress(100,i);
                    //continue;
                }
                publishProgress(3,i+1);
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return size;
    }

    // 在UI线程中执行，在异步线程中调用publishProgress(Progress)后会立即回调onPregressUpdate方法，可以在该方法中更新UI界面上的任务执行进度
    @Override
    protected void onProgressUpdate(Integer... values) {
        if(ins == null)
        {
            ins = MainActivity.GetInstance();
        }

        switch (values[0])
        {
            case 1:
                ins.Log("Waiting for counting files.");
                break;
            case 2:
                ins.UpdateMax(values[1]);
                ins.Log(String.format("get %d files.",values[1]));
                break;
            case 3:
                ins.Update(values[1]);
                break;
            case 100:
                ins.Log(String.format("decry %d fail.",values[1]));
                break;
        }


    }

    // 任务执行完毕调用该方法，在UI线程中执行，更新数据到UI界面
    @Override
    protected void onPostExecute(Integer result) {
        if(ins == null)
        {
            ins = MainActivity.GetInstance();
        }

        ins.Log(String.format("%d files have decrypted.",result));
    }
}
