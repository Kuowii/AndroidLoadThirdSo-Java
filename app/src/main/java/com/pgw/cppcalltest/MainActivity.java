package com.pgw.cppcalltest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView tv;
    EditText textModuleName;
    EditText textFunName;
    Button btnLoad;
    Button btnDecry;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native String JNILoadTest(String moduleName,String funName);
    public native int decryFile(String oldFile,String newFile);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);

        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        tv = findViewById(R.id.sample_text);
        textModuleName = findViewById(R.id.textModuleName);
        textFunName = findViewById(R.id.textFunName);
        btnLoad = findViewById(R.id.btnLoad);
        btnDecry = findViewById(R.id.btnDecry);

        //tv.setText(JNILoadTest("libthird-lib.so","TestC"));

        btnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable moduleName = textModuleName.getText();
                Editable funName = textFunName.getText();
                if(TextUtils.isEmpty(moduleName) || TextUtils.isEmpty(funName))
                {
                    tv.setText( "Invail input." );
                    return;
                }

                String pn = getPackageName();
                CopyFile(moduleName.toString());

                tv.setText(JNILoadTest("/data/user/0/" + pn +"/app_libs" + moduleName.toString(),funName.toString() ));
            }
        });

        btnDecry.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v)
            {
                CheckFiles();
            }
        });
    }

    // 将 so 文件复制到对应的可读取目录
    protected int CopyFile(String path)
    {
        try {
            String localPath = Environment.getExternalStorageDirectory() + path;
            Log.v("Main", "LazyBandingLib localPath:" + localPath);

            String[] tokens = localPath.split("/");
            if (null == tokens || tokens.length <= 0
                    || tokens[tokens.length - 1] == "") {
                Log.v("Main", "非法的文件路径！");
                return -3;
            }
            // 开辟一个输入流
            File inFile = new File(localPath);
            // 判断需加载的文件是否存在
            if (!inFile.exists()) {
                // 下载远程驱动文件
                Log.v("Main", inFile.getAbsolutePath() + " is not fond!");
                return 1;
            }
            FileInputStream fis = new FileInputStream(inFile);

            File dir = this.getDir("libs", Context.MODE_PRIVATE);

            // 获取驱动文件输出流
            File soFile = new File(dir, tokens[tokens.length - 1]);
            if (!soFile.exists()) {
                Log.v("Main", "### " + soFile.getAbsolutePath() + " is not exists");
                FileOutputStream fos = new FileOutputStream(soFile);
                Log.v("Main", "FileOutputStream:" + fos.toString() + ",tokens:"
                        + tokens[tokens.length - 1]);

                // 字节数组输出流，写入到内存中(ram)
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = -1;
                while ((len = fis.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                // 从内存到写入到具体文件
                fos.write(baos.toByteArray());
                // 关闭文件流
                baos.close();
                fos.close();
            }else
            {
                Log.v("Main", soFile.getPath() + " is exists.");
            }
            fis.close();
            //Log.v("Main", "### System.load start");
            // 加载外设驱动
            //System.load(soFile.getAbsolutePath());
            //Log.v("Main", "### System.load End");

            return 0;

        } catch (Exception e) {
            Log.v("Main", "Exception   " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    // 获取指定目录下的所有文件
    protected void CheckFiles()
    {
        List<String> Files = FileHelper.getFilesAllName(Environment.getExternalStorageDirectory() + "/Pictures/Data/Origin");
        List<String> Files_New = new ArrayList<>();
        int size = Files.size();
        for(int i =0;i<size;i++)
        {
            String old = Files.get(i);
            String parentPath = old.substring(old.lastIndexOf("Origin") + 6);
            String newFile = Environment.getExternalStorageDirectory() + "/Pictures/Data/New"+parentPath;
            FileHelper.createFile(newFile);

            int r = decryFile(old,newFile);

            if(r > 0)
            {
                tv.setText(old +" decry fail.");
                break;
            }
        }
    }


    //先定义
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    //权限检测 然后通过一个函数来申请
    public static void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
