package com.pgw.cppcalltest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    TextView tv;
    TextView pbText;
    EditText textModuleName;
    EditText textFunName;
    EditText textKey;
    EditText textPoint;
    Button btnLoad;
    Button btnDecry;
    ProgressBar pbMainDealFiles;

    public int tPoint;
    public int nCount;


    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    /**
     * 单例
     */
    protected static MainActivity _Instance = null;

    public static MainActivity GetInstance()
    {
        if(_Instance==null)
        {
            Log.e("Main", "GetInstance: null");
        }

        return _Instance;
    }

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
    public native int decryFile(String oldFile,String newFile,int point);
    public native int SetKeyAndPoint(String newKey,int newPoint);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        verifyStoragePermissions(this);
        _Instance = this;

        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        tv = findViewById(R.id.sample_text);
        pbText = findViewById(R.id.pbText);
        textModuleName = findViewById(R.id.textModuleName);
        textFunName = findViewById(R.id.textFunName);
        textKey = findViewById(R.id.textKey);
        textPoint = findViewById(R.id.textPoint);
        btnLoad = findViewById(R.id.btnLoad);
        btnDecry = findViewById(R.id.btnDecry);
        pbMainDealFiles = findViewById(R.id.pbMainDealFiles);

        preferences = getSharedPreferences("TestCache", MODE_PRIVATE);
        editor = preferences.edit();
        InitData();

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
                String moduleFileName = CopyFile(moduleName.toString());

                tv.setText(JNILoadTest("/data/user/0/" + pn +"/app_libs" + moduleFileName,funName.toString() ));

                SaveData();
            }
        });

        btnDecry.setOnClickListener(new View.OnClickListener(){
            @Override
            public  void onClick(View v)
            {
                Editable key = textKey.getText();
                Editable point = textPoint.getText();
                if(TextUtils.isEmpty(key) || TextUtils.isEmpty(point))
            {
                tv.setText( "Invail config." );
                return;
            }

                tPoint = Integer.parseInt(point.toString());
                SetKeyAndPoint(key.toString(),tPoint);

                CheckFiles();
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        SaveData();
        super.onDestroy();
    }

    protected  void InitData()
    {
        textModuleName.setText(preferences.getString("ModuleName","/libthird-lib.so"));
        textFunName.setText(preferences.getString("FunName","TestDecryptData"));
        textKey.setText(preferences.getString("Key","v#key"));
        textPoint.setText(preferences.getString("Point","9"));
    }

    protected void SaveData()
    {
        editor.putString("ModuleName",textModuleName.getText().toString());
        editor.putString("FunName",textFunName.getText().toString());
        editor.putString("Key",textKey.getText().toString());
        editor.putString("Point",textPoint.getText().toString());
        if(editor.commit()){
            Toast.makeText(this,"保存成功！", Toast.LENGTH_SHORT).show();
        }
    }

    // 将 so 文件复制到对应的可读取目录
    protected String CopyFile(String path)
    {
        try {
            String localPath = Environment.getExternalStorageDirectory() + path;
            Log.v("Main", "LazyBandingLib localPath:" + localPath);

            String[] tokens = localPath.split("/");
            if (null == tokens || tokens.length <= 0
                    || tokens[tokens.length - 1] == "") {
                Log.v("Main", "非法的文件路径！");
                return "NULL";
            }
            // 开辟一个输入流
            File inFile = new File(localPath);
            // 判断需加载的文件是否存在
            if (!inFile.exists()) {
                // 下载远程驱动文件
                Log.v("Main", inFile.getAbsolutePath() + " is not fond!");
                return "NULL";
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

            return tokens[tokens.length-1];

        } catch (Exception e) {
            Log.v("Main", "Exception   " + e.getMessage());
            e.printStackTrace();
            return e.getMessage();
        }
    }

    // 获取指定目录下的所有文件
    protected void CheckFiles()
    {
        //List<String> Files = FileHelper.getFilesAllName(Environment.getExternalStorageDirectory() + "/Pictures/Data/Origin");
        //nCount = Files.size();
        //pbMainDealFiles.setMax(nCount);
        pbMainDealFiles.setProgress(0);
        DealFilesAsyncTask task = new DealFilesAsyncTask();
        task.execute(Environment.getExternalStorageDirectory() + "/Pictures/Data/Origin");
    }

    /**
     * 输出日志
     */
    public void Log(String info)
    {
        tv.setText(info);
    }

    public void  Update(Integer cur)
    {
        pbText.setText(String.format("%d/%d",cur,nCount));
        pbMainDealFiles.setProgress(cur);
    }

    public void UpdateMax(Integer newMax)
    {
        nCount = newMax;
        pbMainDealFiles.setMax(newMax);
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
